# language: pt

Funcionalidade: Geração de assinatura digital simulada
  Como usuário do Sistema Runner
  Quero que o assinador.jar retorne um Signature FHIR válido
  Para que o CLI possa exibir uma resposta estruturalmente correta

  Contexto:
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos

  Cenário: Resposta contém resourceType correto
    Quando o usuário executa o comando assinar com PIN "1234"
    Então a resposta deve ter resourceType igual a "Signature"

  Cenário: Resposta contém type.code correto conforme perfil SES-GO
    Quando o usuário executa o comando assinar com PIN "1234"
    Então o campo type deve conter code igual a "1.2.840.10065.1.12.1.5"

  Cenário: Resposta contém who apenas com identifier
    Quando o usuário executa o comando assinar com PIN "1234"
    Então o campo who deve conter identifier com system "urn:brasil:cpf"
    E o campo who não deve conter o campo display

  Cenário: Resposta contém sigFormat e targetFormat corretos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então o campo sigFormat deve ser "application/jose"
    E o campo targetFormat deve ser "application/octet-stream"

  Cenário: Campo data contém JWS JSON Serialization válido
    Quando o usuário executa o comando assinar com PIN "1234"
    Então o campo data deve ser decodificável em base64
    E o JWS decodificado deve conter o campo payload
    E o JWS decodificado deve conter signatures com pelo menos uma entrada
    E cada signature deve conter os campos protected, header e signature

  Cenário: Campo when e iat do protected header são consistentes
    Quando o usuário executa o comando assinar com PIN "1234"
    Então o campo when do Signature deve corresponder ao iat do protected header

  Cenário: PIN inválido retorna erro
    Quando o usuário executa o comando assinar com PIN "abc"
    Então deve ser lançada uma exceção com mensagem contendo "PIN inválido"

  Cenário: PIN inválido com menos de 4 dígitos retorna erro
    Quando o usuário executa o comando assinar com PIN "12"
    Então deve ser lançada uma exceção com mensagem contendo "PIN inválido"

  Cenário: PIN nulo retorna erro
    Quando o usuário executa o comando assinar com PIN 'null'
    Então deve ser lançada uma exceção com mensagem contendo "PIN inválido."

  Cenário: Bundle com resourceType inválido retorna erro
    Dado que existe um arquivo Bundle com resourceType "Patient"
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle inválido: resourceType deve ser 'Bundle'."

  Cenário: Bundle sem entradas retorna erro
    Dado que existe um arquivo Bundle vazio
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle não contém nenhuma entrada"

  Cenário: Bundle com fullUrl fora do padrão UUID retorna erro
    Dado que existe um arquivo Bundle com fullUrl inválido
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle inválido: fullUrl ausente ou fora do formato urn:uuid:"

  Cenário: Provenance com resourceType inválido retorna erro
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance com resourceType "Patient"
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Provenance inválido: resourceType deve ser 'Provenance'."

  Cenário: Provenance sem campo recorded retorna erro
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance sem o campo recorded
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Provenance inválido: campo 'recorded' é obrigatório."

  Cenário: Provenance com timestamp em formato não ISO-8601 retorna erro
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance com recorded inválido "2026-15-99"
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Formato de data inválido em 'recorded'"

  Cenário: Provenance com timestamp fora da janela de 5 minutos retorna erro
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance com recorded de 10 minutos atrás
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Timestamp fora da janela permitida"

  Cenário: Validação de assinatura com arquivos corretos retorna sucesso
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Signature válido
    Quando o usuário executa o comando validar assinatura
    Então o resultado da validação deve conter "Assinatura digital validada com sucesso"

  Cenário: Arquivo Bundle inexistente retorna erro
    Dado que o arquivo Bundle não existe
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Arquivo Bundle não encontrado"

  Cenário: Arquivo Bundle com JSON malformado retorna erro
    Dado que existe um arquivo Bundle com JSON inválido
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle não é um JSON válido"

  Cenário: Bundle sem a propriedade entry retorna erro
    Dado que existe um arquivo Bundle sem a propriedade entry
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle não contém nenhuma entrada"

  Cenário: Bundle com propriedade entry que não é um array retorna erro
    Dado que existe um arquivo Bundle com a propriedade entry como objeto
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle não contém nenhuma entrada"

  Cenário: Bundle com entry sem a propriedade fullUrl retorna erro
    Dado que existe um arquivo Bundle com uma entry sem fullUrl
    E que existe um arquivo Provenance com recorded dentro da janela de 5 minutos
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Bundle inválido: fullUrl ausente ou fora do formato"

  Cenário: Arquivo Provenance inexistente retorna erro
    Dado que existe um arquivo Bundle válido
    E que o arquivo Provenance não existe
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Arquivo Provenance não encontrado"

  Cenário: Arquivo Provenance com JSON malformado retorna erro
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance com JSON inválido
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Provenance não é um JSON válido"

  Cenário: Arquivo Provenance com campo recorded em branco retorna erro
    Dado que existe um arquivo Bundle válido
    E que existe um arquivo Provenance com o campo recorded em branco
    Quando o usuário executa o comando assinar com PIN "1234"
    Então deve ser lançada uma exceção com mensagem contendo "Provenance inválido: campo 'recorded' é obrigatório"

  Cenário: Arquivo Signature inexistente retorna erro
    Dado que o arquivo Signature não existe
    E que existe um arquivo Bundle válido
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Arquivo Signature não encontrado"

  Cenário: Arquivo Signature com JSON malformado retorna erro
    Dado que existe um arquivo Signature com JSON inválido
    E que existe um arquivo Bundle válido
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Signature não é um JSON válido"

  Cenário: Arquivo Signature com resourceType inválido retorna erro
    Dado que existe um arquivo Signature com resourceType "Patient"
    E que existe um arquivo Bundle válido
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Signature inválido: resourceType deve ser 'Signature'."

  Cenário: Arquivo Signature sem o campo data retorna erro
    Dado que existe um arquivo Signature sem o campo data
    E que existe um arquivo Bundle válido
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Signature inválido: campo 'data' é obrigatório."

  Cenário: Arquivo Signature com campo data do tipo não-string retorna erro (getTextOrNull false hit)
    Dado que existe um arquivo Signature com campo data numérico
    E que existe um arquivo Bundle válido
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Signature inválido: campo 'data' é obrigatório."

  Cenário: Validação falha se arquivo Bundle não existe
    Dado que existe um arquivo Signature válido
    E que o arquivo Bundle não existe
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Arquivo Bundle não encontrado"

  Cenário: Validação falha se arquivo Bundle possui JSON inválido
    Dado que existe um arquivo Signature válido
    E que existe um arquivo Bundle com JSON inválido
    Quando o usuário executa o comando validar assinatura
    Então deve ser lançada uma exceção com mensagem contendo "Bundle não é um JSON válido"