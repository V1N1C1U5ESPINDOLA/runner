# assinador.jar

Componente Java do **Sistema Runner** responsável por simular operações de assinatura digital no padrão FHIR da SES-GO. Pode ser invocado diretamente via linha de comandos (`java -jar`) ou como servidor HTTP, conforme o modo de uso escolhido pelo CLI.

## Visão geral

O assinador.jar recebe arquivos FHIR (Bundle e Provenance), valida rigorosamente os parâmetros de entrada e retorna uma assinatura simulada no formato `Signature` FHIR. Também suporta validação de assinaturas previamente geradas.

**Stack:** Java 21 · Spring Boot · PicoCLI · Cucumber (testes)

---

## Status do projeto

O progresso detalhado por sprint está no [plano de trabalho](../plano-trabalho.md), mantido atualizado a cada sprint. O resumo abaixo reflete o estado atual do componente:

| Sprint | Foco                                     | Status      |
|--------|------------------------------------------|-------------|
| 1      | Mock FHIR correto + validação de entrada | ✅ Concluído |
| 2      | Testes de integração via `java -jar`     | ✅ Concluído |
| 3      | Endpoints HTTP (`/sign`, `/validate`)    | ✅ Concluído |
| 4      | Integração PKCS#11 com SoftHSM2          | ✅ Concluído |
| 5      | Documentação e qualidade                 | 🔄 Em andamento |
| 6      | CI/CD e releases                         | ⏳ Pendente  |
| 7      | Testes de aceitação e entrega final      | ⏳ Pendente  |

---

## Pré-requisitos

- Java 21+
- Maven 3.9+ (ou use o `./mvnw` incluso no projeto)

Para testes de integração com PKCS#11:

- SoftHSM2 instalado e configurado — consulte o [guia de setup](./docs/SOFTHSM2_SETUP.md)

---

## Como compilar

```bash
./mvnw clean package
```

O jar executável será gerado em `target/assinador-<versão>.jar`.

Para compilar sem rodar os testes:

```bash
./mvnw clean package -DskipTests
```

---

## Como executar

### Modo CLI (invocação direta)

**Criar assinatura:**

```bash
java -jar target/assinador-*.jar assinar \
  --bundle caminho/bundle.json \
  --provenance caminho/provenance.json \
  --pin 1234
```

**Validar assinatura:**

```bash
java -jar target/assinador-*.jar validar \
  --signature caminho/signature.json \
  --bundle caminho/bundle.json
```

A saída de sucesso é escrita em **stdout** como JSON puro. Erros vão para **stdout** como `OperationOutcome` FHIR (exit code 1). Erros inesperados do sistema vão para **stderr**.

Para o formato completo de entrada/saída, consulte o [CONTRATO.md](./docs/CONTRATO.md).

### Modo servidor HTTP

```bash
java -jar target/assinador-*.jar
```

O servidor sobe na porta `8080` por padrão e expõe os endpoints:

| Método | Endpoint    | Descrição                    |
|--------|-------------|------------------------------|
| POST   | `/sign`     | Criar assinatura simulada    |
| POST   | `/validate` | Validar assinatura existente |

---

## Como rodar os testes

```bash
./mvnw test
```

Os testes estão organizados com Cucumber e cobrem três camadas:

- **Unitários** (`assinatura.feature`) — valida o `SignatureService` diretamente
- **Integração CLI** (`integracao-cli.feature`) — invoca o `java -jar` real via `ProcessBuilder`
- **HTTP** (`http.feature`) — valida os endpoints REST
- **PKCS#11** (`pkcs11.feature`) — requer SoftHSM2 configurado; ignorados automaticamente se ausente

Para rodar apenas os testes sem PKCS#11:

```bash
./mvnw test -Dcucumber.filter.tags="not @pkcs11"
```

---

## Estrutura do projeto

```
src/
├── main/java/.../assinador/
│   ├── Application.java               # Ponto de entrada
│   ├── cli/                           # Comandos PicoCLI (assinar, validar)
│   └── core/
│       ├── controller/                # Endpoints HTTP
│       ├── exception/                 # AssinadorException + GlobalExceptionHandler
│       ├── model/dto/                 # Records FHIR (Signature, OperationOutcome, JWS...)
│       ├── model/enums/               # Códigos de situação excepcional
│       ├── model/factory/             # OperationOutcomeFactory
│       └── service/                   # SignatureService, Pkcs11Service
└── test/
    ├── steps/                         # Step definitions Cucumber
    └── resources/features/            # Cenários .feature
```

---

## Referências

- [Contrato CLI ↔ assinador.jar](./docs/CONTRATO.md)
- [Setup do SoftHSM2](./docs/SOFTHSM2_SETUP.md)
- [Plano de trabalho completo](../plano-trabalho.md)
- [Caso de uso: Criar Assinatura — SES-GO](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-criar-assinatura.html)
- [Caso de uso: Validar Assinatura — SES-GO](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-validar-assinatura.html)