# language: pt

Funcionalidade: Integração PKCS#11 com SoftHSM2
  Como usuário do Sistema Runner
  Quero que o assinador.jar extraia o CPF do certificado no token PKCS#11
  Para que a identidade do signatário reflita o certificado real

  @pkcs11
  Cenário: CPF extraído do token substitui o valor fixo simulado
    Dado que o SoftHSM2 está disponível
    Quando o serviço PKCS#11 tenta extrair o CPF
    Então o CPF retornado não deve ser o valor fixo "00000000000"
    E o CPF retornado deve ter 11 dígitos

  @pkcs11
  Cenário: CPF simulado usado quando SoftHSM2 não está disponível
    Dado que o SoftHSM2 não está disponível
    Quando o serviço PKCS#11 tenta extrair o CPF
    Então o resultado deve ser vazio