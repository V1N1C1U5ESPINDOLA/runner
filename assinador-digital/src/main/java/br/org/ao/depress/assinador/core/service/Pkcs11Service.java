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

@Slf4j
@Service
public class Pkcs11Service {

    private static final String PIN = System.getenv().getOrDefault("PKCS11_PIN", "1234");

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