# language: pt

Funcionalidade: Endpoints HTTP do assinador.jar (modo servidor)
  Como usuário do Sistema Runner
  Quero invocar o assinador via HTTP
  Para que o CLI possa usar o modo servidor com menor latência

  Contexto:
    Dado que existe um arquivo Bundle válido para HTTP
    E que existe um arquivo Provenance válido para HTTP

  Cenário: POST /sign com parâmetros válidos retorna Signature FHIR
    Quando o cliente envia POST para "/sign" com PIN "1234"
    Então a resposta HTTP deve ter status 200
    E o corpo da resposta deve ter resourceType igual a "Signature"
    E o corpo da resposta deve ter sigFormat igual a "application/jose"

  Cenário: POST /sign com PIN inválido retorna 422 com OperationOutcome
    Quando o cliente envia POST para "/sign" com PIN "abc"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "CRYPTO.PIN-INVALID"

  Cenário: POST /sign com Bundle inexistente retorna 422 com OperationOutcome
    Dado que o Bundle para HTTP não existe
    Quando o cliente envia POST para "/sign" com PIN "1234"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"

  Cenário: POST /validate com Signature válido retorna OperationOutcome de sucesso
    Dado que existe um arquivo Signature gerado via HTTP
    Quando o cliente envia POST para "/validate"
    Então a resposta HTTP deve ter status 200
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "VALIDATION.SUCCESS"

  Cenário: POST /validate com Signature inexistente retorna 422
    Dado que o Signature para HTTP não existe
    Quando o cliente envia POST para "/validate"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"

  Cenário: POST /sign com Provenance fora da janela de tempo retorna 422
    Dado que existe um arquivo Provenance com recorded de 10 minutos atrás para HTTP
    Quando o cliente envia POST para "/sign" com PIN "1234"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.INVALID-TIMESTAMP"

  Cenário: POST /sign com Bundle com UUID inválido retorna 422
    Dado que existe um arquivo Bundle com UUID inválido para HTTP
    Quando o cliente envia POST para "/sign" com PIN "1234"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.UUID-INVALID"

  Cenário: POST /sign com Provenance com JSON inválido retorna 422
    Dado que existe um arquivo Provenance com JSON inválido para HTTP
    Quando o cliente envia POST para "/sign" com PIN "1234"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.JSON-MALFORMED"

  Cenário: POST /validate com Signature com resourceType inválido retorna 422
    Dado que existe um arquivo Signature com resourceType "Patient" para HTTP
    Quando o cliente envia POST para "/validate"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.JWS-MALFORMED"

  Cenário: POST /validate com Signature sem campo data retorna 422
    Dado que existe um arquivo Signature sem o campo data para HTTP
    Quando o cliente envia POST para "/validate"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.JWS-MALFORMED"

  Cenário: POST /validate com Signature com JSON inválido retorna 422
    Dado que existe um arquivo Signature com JSON inválido para HTTP
    Quando o cliente envia POST para "/validate"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.JSON-MALFORMED"

  Cenário: POST /validate com Bundle com JSON inválido retorna 422
    Dado que existe um arquivo Signature gerado via HTTP
    E que existe um arquivo Bundle com JSON inválido para HTTP
    Quando o cliente envia POST para "/validate"
    Então a resposta HTTP deve ter status 422
    E o corpo da resposta deve ter resourceType igual a "OperationOutcome"
    E o corpo da resposta deve conter o código "FORMAT.JSON-MALFORMED"