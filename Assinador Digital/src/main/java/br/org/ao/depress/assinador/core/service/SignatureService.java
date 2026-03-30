package br.org.ao.depress.assinador.core.service;

import br.org.ao.depress.assinador.core.model.dto.ProvenanceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final ObjectMapper objectMapper;

    public String executarAssinatura(File bundleFile, File provenanceFile, String pin) {

        if (!bundleFile.exists() || !provenanceFile.exists()) {
            throw new IllegalArgumentException("Arquivos Bundle ou Provenance não encontrados.");
        }
        validarPin(pin);

        ProvenanceDTO provenance = objectMapper.readValue(provenanceFile, ProvenanceDTO.class);

        if (provenance.recorded() == null || provenance.recorded().isBlank()) {
            throw new IllegalArgumentException("O campo 'recorded' é obrigatório no Provenance.");
        }

        validarJanelaDeTempo(provenance.recorded());

        return gerarMockSignature();
    }

    //TODO fazer implementação completa
    public boolean validarAssinatura(File signatureJson, File originalBundle) {

        if (!signatureJson.exists() || !originalBundle.exists()) {
            throw new IllegalArgumentException("Arquivos de entrada não encontrados.");
        }

        return !originalBundle.getName().contains("invalid");
    }

    private void validarPin(String pin) {
        if (pin == null || !pin.matches("\\d{4,8}")) {
            throw new IllegalArgumentException("PIN inválido. Deve conter apenas entre 4 e 8 números.");
        }
    }

    private void validarJanelaDeTempo(String recordedStr) {
        try {
            OffsetDateTime dataDocumento = OffsetDateTime.parse(recordedStr);
            OffsetDateTime dataAtual = OffsetDateTime.now(ZoneOffset.UTC);

            long diferencaEmMinutos = Math.abs(ChronoUnit.MINUTES.between(dataAtual, dataDocumento));

            if (diferencaEmMinutos > 5) {
                throw new IllegalStateException(
                        "Timestamp fora da janela permitida. O documento está atrasado ou adiantado em "
                                + diferencaEmMinutos + " minutos (Limite é 5)."
                );
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use ISO-8601 (ex: 2026-03-30T14:30:00Z).");
        }
    }

    private String gerarMockSignature() {
        return """
        {
          "resourceType": "Signature",
          "type": [{ "system": "urn:iso-astm:E1762-95:2013", "code": "1.2.840.10065.1.12.1.1" }],
          "when": "%s",
          "who": { "display": "Simulador Runner" },
          "data": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.c2ltdWxhY2Fv.YXNzaW5hdHVyYQ=="
        }
        """.formatted(OffsetDateTime.now().toString());
    }
}