package br.org.ao.depress.assinador.core.service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Serviço responsável pela integração com dispositivo criptográfico via protocolo PKCS#11.
 *
 * <p>Utiliza o provider {@code SunPKCS11} do Java, configurado a partir do arquivo
 * {@code pkcs11.cfg} no classpath, para acessar o token e extrair o CPF do signatário
 * a partir do campo {@code serialNumber} do certificado armazenado no dispositivo.
 *
 * <p>Em ambiente de desenvolvimento e testes, o dispositivo físico é substituído pelo
 * {@code SoftHSM2} — consulte o <a href="../SOFTHSM2_SETUP.md">guia de setup</a>.
 *
 * <p>Todos os métodos desta classe são tolerantes a falhas: qualquer erro de inicialização
 * ou acesso ao dispositivo resulta em {@link Optional#empty()}, permitindo que o
 * {@link SignatureService} opere normalmente com CPF simulado como fallback.
 *
 * <p>O PIN é lido da variável de ambiente {@code PKCS11_PIN}. Se não definida, o valor
 * padrão {@code "1234"} é usado (adequado apenas para testes com SoftHSM2).
 */
@Slf4j
@Service
public class Pkcs11Service {

    private static final String PIN = System.getenv().getOrDefault("PKCS11_PIN", "1234");

    /**
     * Tenta extrair o CPF do signatário a partir do certificado presente no dispositivo PKCS#11.
     *
     * <p>O fluxo é: carregar o provider SunPKCS11 → abrir o {@link KeyStore} com o PIN →
     * iterar os aliases → localizar o primeiro certificado cujo {@code serialNumber} contenha
     * um CPF no formato {@code CPF:<número>}.
     *
     * @return {@link Optional} com o CPF (somente dígitos, sem prefixo {@code CPF:}) se encontrado;
     *         {@link Optional#empty()} se o dispositivo não estiver disponível, nenhum certificado
     *         com CPF for encontrado, ou qualquer erro ocorrer durante o acesso
     */
    public Optional<String> extrairCpf() {
        try {
            Optional<Provider> providerOpt = carregarProvider();
            if (providerOpt.isEmpty()) return Optional.empty();

            KeyStore ks = KeyStore.getInstance("PKCS11", providerOpt.get());
            ks.load(null, PIN.toCharArray());

            var aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isCertificateEntry(alias) || ks.isKeyEntry(alias)) {
                    var cert = (X509Certificate) ks.getCertificate(alias);
                    if (cert == null) continue;

                    Optional<String> cpf = extrairCpfDoCertificado(cert);
                    if (cpf.isPresent()) return cpf;
                }
            }

            log.warn("PKCS#11: nenhum certificado com CPF encontrado no token.");
            return Optional.empty();

        } catch (Exception e) {
            log.warn("PKCS#11 indisponível — usando CPF simulado. Motivo: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Carrega e registra o provider {@code SunPKCS11} a partir do arquivo de configuração
     * {@code pkcs11.cfg} presente no classpath.
     *
     * <p>O {@code SunPKCS11} exige um caminho de arquivo físico — não aceita {@link InputStream}
     * diretamente. Por isso, o recurso do classpath é copiado para um arquivo temporário antes
     * de ser passado ao método {@code configure()}, que é então removido após o carregamento.
     *
     * @return {@link Optional} com o provider registrado, ou {@link Optional#empty()} se
     *         {@code pkcs11.cfg} não for encontrado ou o carregamento falhar
     */
    private Optional<Provider> carregarProvider() {
        InputStream config = getClass().getResourceAsStream("/pkcs11.cfg");
        if (config == null) {
            log.warn("pkcs11.cfg não encontrado no classpath.");
            return Optional.empty();
        }

        try {
            Path tempConfig = Files.createTempFile("pkcs11", ".cfg");
            Files.copy(config, tempConfig, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Provider provider = Security.getProvider("SunPKCS11");
            provider = provider.configure(tempConfig.toString());
            Security.addProvider(provider);

            Files.delete(tempConfig);
            return Optional.of(provider);
        } catch (Exception e) {
            log.warn("Falha ao carregar SunPKCS11: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extrai o CPF do campo {@code serialNumber} do Subject de um certificado X.509.
     *
     * <p>Certificados ICP-Brasil armazenam o CPF no {@code serialNumber} com o prefixo
     * {@code CPF:} (ex.: {@code CPF:98765432100}). Este método usa Bouncy Castle para
     * parsear o Distinguished Name e localizar esse campo.
     *
     * @param cert certificado X.509 a ser inspecionado
     * @return {@link Optional} com o CPF sem o prefixo {@code CPF:}, ou {@link Optional#empty()}
     *         se o campo não existir ou não seguir o formato esperado
     */
    private Optional<String> extrairCpfDoCertificado(X509Certificate cert) {
        try {
            String dn = cert.getSubjectX500Principal().getName();
            X500Name x500Name = new X500Name(dn);

            var rdns = x500Name.getRDNs(BCStyle.SERIALNUMBER);
            if (rdns.length == 0) return Optional.empty();

            String serialNumber = rdns[0].getFirst().getValue().toString();

            if (serialNumber.startsWith("CPF:")) {
                return Optional.of(serialNumber.substring(4));
            }

            return Optional.empty();
        } catch (Exception e) {
            log.warn("Falha ao extrair CPF do certificado: {}", e.getMessage());
            return Optional.empty();
        }
    }
}