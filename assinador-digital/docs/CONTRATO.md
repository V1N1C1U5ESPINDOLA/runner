# Contrato de Integração — CLI ↔ assinador.jar

Este documento define o contrato de comunicação entre o CLI (`assinatura`, em Go)
e o `assinador.jar` (Java), no modo de invocação direta (local).

## Comandos

### Criar assinatura

```bash
java -jar assinador.jar assinar \
  --bundle <caminho do arquivo JSON Bundle> \
  --provenance <caminho do arquivo JSON Provenance> \
  --pin <PIN numérico de 4 a 8 dígitos>
```

### Validar assinatura

```bash
java -jar assinador.jar validar \
  --signature <caminho do arquivo JSON Signature> \
  --bundle <caminho do arquivo JSON Bundle original>
```

## Saída

### Sucesso (exit code 0)

A saída é escrita em **stdout**, como JSON válido e indentado.

Para `assinar`, o JSON é um recurso FHIR `Signature`:

```json
{
  "resourceType": "Signature",
  "type": [{ "system": "urn:iso-astm:E1762-95:2013", "code": "1.2.840.10065.1.12.1.5" }],
  "when": "2026-05-05T12:00:00Z",
  "who": { "identifier": { "system": "urn:brasil:cpf", "value": "00000000000" } },
  "sigFormat": "application/jose",
  "targetFormat": "application/octet-stream",
  "data": "<base64>"
}
```

Para `validar`, o JSON é um recurso FHIR `OperationOutcome`:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [{
    "severity": "information",
    "code": "informational",
    "details": {
      "coding": [{
        "system": "https://fhir.saude.go.gov.br/r4/seguranca/CodeSystem/situacao-excepcional-assinatura",
        "code": "VALIDATION.SUCCESS",
        "display": "Validação Bem-sucedida"
      }],
      "text": "Validação Bem-sucedida"
    },
    "diagnostics": "Assinatura digital validada com sucesso"
  }]
}
```

### Erro (exit code 1)

A saída de erro é escrita em **stdout** como um `OperationOutcome` com `severity: "error"`:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [{
    "severity": "error",
    "code": "invalid",
    "details": {
      "coding": [{
        "system": "https://fhir.saude.go.gov.br/r4/seguranca/CodeSystem/situacao-excepcional-assinatura",
        "code": "CRYPTO.PIN-INVALID",
        "display": "PIN Inválido"
      }],
      "text": "PIN Inválido"
    },
    "diagnostics": "PIN inválido. Deve conter apenas entre 4 e 8 dígitos numéricos."
  }]
}
```

Erros inesperados do sistema (falha de I/O, exceção não tratada) são escritos em **stderr**
como texto simples, também com exit code 1.

## Códigos de erro disponíveis

| Código                      | Situação                                                          |
|-----------------------------|-------------------------------------------------------------------|
| `FORMAT.JSON-MALFORMED`     | Arquivo não é um JSON válido                                      |
| `FORMAT.BUNDLE-MALFORMED`   | Bundle ausente ou resourceType incorreto                          |
| `FORMAT.BUNDLE-EMPTY`       | Bundle sem entradas                                               |
| `FORMAT.UUID-INVALID`       | fullUrl fora do formato `urn:uuid:<UUID>`                         |
| `FORMAT.PROVENANCE-INVALID` | Provenance ausente ou resourceType incorreto                      |
| `FORMAT.INVALID-TIMESTAMP`  | Campo `recorded` fora da janela de ±5 minutos ou formato inválido |
| `FORMAT.JWS-MALFORMED`      | Signature ausente, resourceType incorreto ou campo `data` ausente |
| `CRYPTO.PIN-INVALID`        | PIN nulo, não numérico ou fora de 4–8 dígitos                     |

## Observações

- Todos os caminhos de arquivo devem ser absolutos ou relativos ao diretório de execução.
- O campo `recorded` do Provenance deve estar em formato ISO-8601 e dentro de ±5 minutos do horário atual.
- O stdout é sempre JSON puro, sem prefixos de log. Logs internos do Spring vão para stderr.