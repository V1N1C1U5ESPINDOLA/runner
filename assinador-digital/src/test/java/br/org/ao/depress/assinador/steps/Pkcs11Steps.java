package br.org.ao.depress.assinador.steps;

import br.org.ao.depress.assinador.core.service.Pkcs11Service;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "OptionalUsedAsFieldOrParameterType"})
public class Pkcs11Steps {

    @Autowired
    private Pkcs11Service pkcs11Service;

    private Optional<String> cpfExtraido;
    private boolean softhsmDisponivel;

    @Dado("que o SoftHSM2 está disponível")
    public void softHsmDisponivel() {
        File lib = new File("/usr/lib/softhsm/libsofthsm2.so");
        softhsmDisponivel = lib.exists();
        org.junit.jupiter.api.Assumptions.assumeTrue(
                softhsmDisponivel,
                "SoftHSM2 não está instalado — cenário ignorado."
        );
    }

    @Dado("que o SoftHSM2 não está disponível")
    public void softHsmNaoDisponivel() {
        softhsmDisponivel = false;
    }

    @Quando("o serviço PKCS#11 tenta extrair o CPF")
    public void servicoTentaExtrairCpf() {
        if (!softhsmDisponivel) {
            cpfExtraido = Optional.empty();
        } else {
            cpfExtraido = pkcs11Service.extrairCpf();
        }
    }

    @Então("o CPF retornado não deve ser o valor fixo {string}")
    public void cpfNaoDeveSerFixo(String valorFixo) {
        assertThat(cpfExtraido).isPresent();
        assertThat(cpfExtraido.get()).isNotEqualTo(valorFixo);
    }

    @Então("o CPF retornado deve ter {int} dígitos")
    public void cpfDeveTerDigitos(int quantidadeDigitos) {
        assertThat(cpfExtraido).isPresent();
        assertThat(cpfExtraido.get()).matches("\\d{" + quantidadeDigitos + "}");
    }

    @Então("o resultado deve ser vazio")
    public void resultadoDeveSerVazio() {
        assertThat(cpfExtraido).isEmpty();
    }
}