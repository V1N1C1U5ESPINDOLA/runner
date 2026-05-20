# Setup do SoftHSM2 para desenvolvimento e testes

O SoftHSM2 é um simulador de dispositivo criptográfico (token/smart card) usado nos
testes de integração PKCS#11 do assinador.jar. Este guia descreve como configurá-lo.

## Pré-requisitos

- Ubuntu/Debian (ou derivado)
- Java 21+
- Maven

## Instalação

```bash
sudo apt install softhsm2 openssl pkcs11-provider
```

## Configuração e inicialização

Execute o script de setup incluído no projeto:

```bash
./setup-softhsm.sh
```

O script realiza automaticamente:
1. Cria a configuração do SoftHSM2 em `~/.config/softhsm2/softhsm2.conf`
2. Inicializa um token chamado `MeuToken` com PIN `1234`
3. Gera um par de chaves RSA 2048 no token
4. Cria e importa um certificado autoassinado com CPF no campo Subject

O setup é idempotente — pode ser executado múltiplas vezes sem efeitos colaterais.

## Configuração do PIN

O PIN padrão de teste é `1234`. Em ambientes que exigem outro valor, defina:

```bash
export PKCS11_PIN=seu-pin
```

## Verificação

Após o setup, verifique se o token está configurado corretamente:

```bash
pkcs11-tool --module /usr/lib/softhsm/libsofthsm2.so -L
pkcs11-tool --module /usr/lib/softhsm/libsofthsm2.so --slot 0 --login --pin 1234 -O
```

Deve aparecer a chave privada e o certificado com label `minha-chave`.

## Rodando os testes

Com o SoftHSM2 configurado, os testes de integração PKCS#11 rodam normalmente:

```bash
mvn test
```

Testes marcados com `@pkcs11` são ignorados automaticamente se o SoftHSM2
não estiver instalado — não quebram o build em ambientes sem o simulador.

## Comportamento sem SoftHSM2

Quando o dispositivo PKCS#11 não está disponível, o assinador.jar:
- Continua funcionando normalmente
- Usa CPF simulado fixo (`00000000000`) no campo `who.identifier.value`
- Registra aviso em stderr informando que o dispositivo não está acessível

## Detalhes do certificado de teste

| Campo        | Valor              |
|--------------|--------------------|
| CN           | Fulano             |
| O            | Empresa            |
| serialNumber | CPF:98765432100    |
| Validade     | 10 anos            |
| Algoritmo    | RSA 2048 / SHA-256 |
| PIN          | 1234               |
| Label        | minha-chave        |