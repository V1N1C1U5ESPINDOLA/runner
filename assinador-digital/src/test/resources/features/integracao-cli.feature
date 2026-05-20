# language: pt

Funcionalidade: Integração ponta-a-ponta via linha de comando
  Como usuário do Sistema Runner
  Quero executar o assinador.jar diretamente via java -jar
  Para que o fluxo completo funcione sem interação com código Java

  Cenário: Assinar com parâmetros válidos retorna Signature FHIR no stdout
    Dado que o jar do assinador está disponível
    E que existe um arquivo Bundle válido para integração
    E que existe um arquivo Provenance válido para integração
    Quando o usuário executa o processo assinar com PIN "1234"
    Então o processo deve encerrar com código 0
    E o stdout deve conter o campo resourceType igual a "Signature"
    E o stdout deve conter o campo sigFormat igual a "application/jose"
    E o stdout deve conter o campo type.code igual a "1.2.840.10065.1.12.1.5"

  Cenário: Assinar com PIN inválido retorna código de saída 1
    Dado que o jar do assinador está disponível
    E que existe um arquivo Bundle válido para integração
    E que existe um arquivo Provenance válido para integração
    Quando o usuário executa o processo assinar com PIN "abc"
    Então o processo deve encerrar com código 1

  Cenário: Assinar com Bundle inexistente retorna código de saída 1
    Dado que o jar do assinador está disponível
    E que o Bundle para integração não existe
    E que existe um arquivo Provenance válido para integração
    Quando o usuário executa o processo assinar com PIN "1234"
    Então o processo deve encerrar com código 1

  Cenário: Assinar com Provenance fora da janela de tempo retorna código de saída 1
    Dado que o jar do assinador está disponível
    E que existe um arquivo Bundle válido para integração
    E que existe um arquivo Provenance com recorded de 10 minutos atrás para integração
    Quando o usuário executa o processo assinar com PIN "1234"
    Então o processo deve encerrar com código 1

  Cenário: Validar assinatura gerada retorna OperationOutcome de sucesso no stdout
    Dado que o jar do assinador está disponível
    E que existe um arquivo Bundle válido para integração
    E que existe um arquivo Provenance válido para integração
    E que existe um arquivo Signature gerado pelo processo assinar
    Quando o usuário executa o processo validar
    Então o processo deve encerrar com código 0
    E o stdout deve conter o campo resourceType igual a "OperationOutcome"
    E o stdout deve conter o texto "Assinatura digital validada com sucesso"

  Cenário: Validar com Signature inexistente retorna código de saída 1
    Dado que o jar do assinador está disponível
    E que existe um arquivo Bundle válido para integração
    E que o Signature para integração não existe
    Quando o usuário executa o processo validar
    Então o processo deve encerrar com código 1

  @pkcs11
  Cenário: Assinatura gerada contém CPF do token no campo who
    Dado que o SoftHSM2 está disponível
    E que existe um arquivo Bundle válido para integração
    E que existe um arquivo Provenance válido para integração
    Quando o usuário executa o processo assinar com PIN "1234"
    Então o processo deve encerrar com código 0
    E o stdout deve conter o campo resourceType igual a "Signature"
    E o stdout não deve conter "00000000000"