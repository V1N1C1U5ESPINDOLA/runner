# Plano de Trabalho — Sistema Runner

> **Especificação upstream (referência fixa):**
> https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md
>
> As histórias abaixo são subdivisões operacionais dos épicos US-01 a US-05 definidos na especificação acima. Os enunciados originais ("Como... Quero... Para que...") e critérios de aceitação da spec pertencem ao repositório upstream e são referenciados, não duplicados.

## Premissas

- **CLI (assinatura e simulador):** Go, desenvolvido pelo @V1N1C1U5ESPINDOLA.
- **assinador.jar:** Java 21 + Spring Boot + PicoCLI, desenvolvido pelo @carlosmorais01.
- **Estratégia:** iterativa e incremental, organizada em **7 sprints de 1 semana** (terça a terça).
- **Divisão:** @V1N1C1U5ESPINDOLA foca nos dois CLIs em Go; @carlosmorais01 foca no assinador.jar. Tarefas de CI/CD e integração são conjuntas.

---

## Rastreabilidade Épicos → Histórias

| Épico | Histórias derivadas |
|-------|---------------------|
| [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01) | US-01.1, US-01.2, US-01.3, US-01.4, US-01.5, US-01.6, US-01.7, US-01.8, US-01.9 |
| [US-02](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-02) | US-02.1, US-02.2, US-02.3, US-02.4, US-02.5 |
| [US-03](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-03) | US-03.1, US-03.2, US-03.3, US-03.4 |
| [US-04](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-04) | US-04.1 |
| [US-05](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-05) | US-05.1, US-05.2, US-05.3 |

---

## Sprint 0 — Estruturação Base (07/04 – 14/04)

**Objetivo:** Consolidar o protótipo já desenvolvido, documentar o estado atual e alinhar a equipe antes do desenvolvimento incremental.

**Tarefa conjunta**
- [x] Código do protótipo commitado e organizado no repositório
- [x] README inicial descrevendo o estado atual do projeto
- [x] Decisões de arquitetura registradas (estrutura de pacotes Go, estrutura Spring Boot)
- [x] Backlog revisado e critérios de aceitação confirmados para Sprint 1
- [x] Plano de trabalho configurado e organizado

---

## Sprint 1 — Fundação dos projetos (21/04 – 28/04)

**Objetivo:** Estrutura base do CLI em Go funcionando. Consolidar e completar o assinador.jar — validação de parâmetros e mock da resposta no formato FHIR correto.

**Valor entregue:** Os dois projetos compilam, rodam e têm os contratos de entrada/saída bem definidos entre si.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-01.1 — Estrutura base do CLI

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [x] Projeto Go inicializado com `go mod init`
- [x] Cobra CLI instalado
- [x] Estrutura de pacotes definida e documentada
- [x] Comando `assinatura version` exibe a versão atual do CLI
- [x] Aplicação compila e executa nas três plataformas (Windows, Linux, macOS)

#### US-01.2 — Parsing de comandos e parâmetros no CLI

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [x] CLI aceita o comando `assinar` com os parâmetros: `--pin`, `--bundle`, `--provenance`
- [x] CLI aceita o comando `validar` com os parâmetros: `--signature`, `--bundle`
- [x] Mensagem de ajuda (`--help`) documenta os comandos e parâmetros disponíveis
- [x] Parâmetros ausentes ou inválidos geram mensagem de erro orientativa
- [x] Testes cobrem o parsing de comandos e parâmetros

---

### @carlosmorais01 — assinador.jar (Java)

#### US-02.1 — Mock da resposta de assinatura no formato FHIR correto

> Épico: [US-02](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-02)

**Critérios de aceitação:**
- [x] `gerarMockSignature()` retorna `Signature` FHIR com todos os campos obrigatórios conforme spec SES-GO
- [x] `type.code` é `"1.2.840.10065.1.12.1.5"` (valor fixado pelo perfil)
- [x] `when` e `iat` do protected header contêm o mesmo instante
- [x] `who` contém apenas `identifier` — sem `reference`, `type` ou `display`
- [x] O campo `data` contém um JWS JSON Serialization em base64 válido (estrutura correta, valores simulados)
- [x] Testes unitários verificam a presença e formato de todos os campos obrigatórios

#### US-02.2 — Validação completa de parâmetros de criação de assinatura

> Épico: [US-02](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-02)

**Critérios de aceitação:**
- [x] Arquivo Bundle existe, é um JSON válido com `resourceType: "Bundle"` e pelo menos uma entry
- [x] Cada entry do Bundle tem `fullUrl` no formato `urn:uuid:<UUID>`
- [x] Arquivo Provenance existe, é um JSON válido com `resourceType: "Provenance"` e campo `recorded`
- [x] `Provenance.recorded` está em formato ISO-8601 válido e dentro da janela de ±5 minutos
- [x] PIN contém apenas dígitos e tem entre 4 e 8 caracteres
- [x] Erros retornam `OperationOutcome` FHIR com código específico do CodeSystem SES-GO (ex: `CRYPTO.PIN-INVALID`, `FORMAT.BUNDLE-EMPTY`)
- [x] Testes unitários cobrem todos os cenários de validação (sucesso e falha)

#### US-02.3 — Simulação e validação de parâmetros de validação de assinatura

> Épico: [US-02](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-02)

**Critérios de aceitação:**
- [x] Arquivo Signature existe e é um JSON com `resourceType: "Signature"` e campo `data` presente
- [x] Arquivo Bundle original existe e é um JSON válido
- [x] Sucesso retorna `OperationOutcome` com código `VALIDATION.SUCCESS`
- [x] Erro retorna `OperationOutcome` com código correspondente à falha
- [x] Testes unitários cobrem cenários de sucesso e falha

---

### Tarefa conjunta — Contrato CLI ↔ assinador.jar

- [x] Contrato formalizado em `CONTRATO.md` no repositório
- [x] Flags, formato de stdout (sucesso) e stdout/stderr (erro) e exit codes documentados

---

## Sprint 2 — Fluxo ponta-a-ponta modo local (28/04 – 05/05)

**Objetivo:** Usuário executa `assinatura assinar ...` e obtém uma assinatura FHIR simulada sem digitar nenhum comando Java manualmente.

**Valor entregue:** Caso de uso principal funcionando de ponta a ponta.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-01.3 — Invocação local do assinador.jar

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] CLI localiza o `java` disponível no PATH (ou provisionado — ver Sprint 3)
- [ ] CLI constrói e executa `java -jar assinador.jar` com os parâmetros corretamente mapeados
- [ ] Saída stdout do assinador.jar é capturada e repassada ao usuário
- [ ] Erros (JDK ausente, jar não encontrado, código de saída != 0) são tratados com mensagens claras
- [ ] Testes de integração validam o fluxo CLI → assinador.jar

#### US-01.4 — Exibição legível de resultados

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] Resultado de criação de assinatura é formatado de forma legível (JSON indentado ou resumo)
- [ ] Resultado de validação indica claramente se a assinatura é válida ou inválida
- [ ] Erros do assinador.jar são apresentados de forma clara ao usuário

---

### @carlosmorais01 — assinador.jar (Java)

#### Testes de integração via linha de comando

**Critérios de aceitação:**
- [x] Fluxo completo `assinar` funciona corretamente via `java -jar` com os argumentos definidos no contrato
- [x] Fluxo completo `validar` funciona corretamente via `java -jar`
- [x] Testes de integração cobrindo o fluxo completo (assinar e validar) via invocação de linha de comando com `ProcessBuilder`

---

## Sprint 3 — Provisionamento de JDK (05/05 – 12/05)

**Objetivo:** CLI detecta, baixa e configura o JDK automaticamente quando ausente.

**Valor entregue:** US-04 completo. Elimina pré-requisito de instalação manual do Java.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-04.1 — Detecção e download automático do JDK

> Épico: [US-04](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-04)

**Critérios de aceitação:**
- [ ] CLI detecta se o JDK está presente na versão exigida (PATH e/ou `~/.hubsaude/jdk/`)
- [ ] CLI baixa o JDK compatível para a plataforma corrente quando ausente
- [ ] JDK baixado é armazenado em `~/.hubsaude/jdk/` e usado nas invocações do assinador.jar
- [ ] Download funciona nas três plataformas
- [ ] Feedback exibido ao usuário durante o download

---

### @carlosmorais01 — assinador.jar (Java)

#### US-02.4 — Endpoints HTTP do assinador.jar (modo servidor)

> Épico: [US-02](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-02)

**Critérios de aceitação:**
- [x] `SignatureController` implementado com `POST /sign` e `POST /validate`
- [x] Endpoints reutilizam a mesma lógica de validação e simulação já existente
- [x] Respostas HTTP seguem estrutura consistente: sucesso (200 + JSON do `Signature`) e erro (4xx + `OperationOutcome`)
- [x] Testes de integração validam os endpoints HTTP

---

## Sprint 4 — Modo servidor e gerenciamento de ciclo de vida (12/05 – 19/05)

**Objetivo:** CLI inicia, detecta, usa e encerra o assinador.jar no modo servidor HTTP.

**Valor entregue:** Menor latência disponível (warm start). CLI gerencia o ciclo de vida completo do assinador.jar.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-01.5 — Iniciar assinador.jar no modo servidor

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] CLI inicia o assinador.jar como processo em background na porta padrão
- [ ] PID e porta do processo são registrados em `~/.hubsaude/assinador.pid`
- [ ] Feedback exibido ao usuário confirmando que o servidor iniciou
- [ ] Porta pode ser personalizada via parâmetro `--port`

#### US-01.6 — Invocar assinador.jar via HTTP

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] CLI envia requisições HTTP para `/sign` e `/validate`
- [ ] Modo servidor é utilizado por padrão quando há instância em execução
- [ ] Flag `--local` força uso do modo local (invocação direta)
- [ ] Testes de integração validam o fluxo CLI → HTTP → assinador.jar

#### US-01.7 — Detectar instância do assinador.jar em execução

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] CLI consulta `~/.hubsaude/assinador.pid` para verificar processo registrado
- [ ] Health check HTTP confirma que o processo está respondendo
- [ ] Se instância ativa encontrada, CLI a reutiliza em vez de iniciar nova
- [ ] Se processo registrado não responde, é considerado inativo

#### US-01.8 — Interromper execução do assinador.jar

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] Comando `assinatura stop` encerra o assinador.jar na porta padrão
- [ ] Parâmetro `--port` permite especificar a porta do processo a encerrar
- [ ] Feedback exibido confirmando o encerramento
- [ ] Registro em `~/.hubsaude/` é atualizado após encerramento

#### US-01.9 — Agendar interrupção por inatividade

> Épico: [US-01](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-01)

**Critérios de aceitação:**
- [ ] Parâmetro `--timeout <minutos>` define tempo máximo de inatividade ao iniciar o servidor
- [ ] Após o período sem requisições, assinador.jar é encerrado automaticamente
- [ ] Mecanismo de timeout documentado no `--help` do CLI

---

### @carlosmorais01 — assinador.jar (Java)

#### US-02.5 — Integração com dispositivo criptográfico via PKCS#11 (SoftHSM2)

> Épico: [US-02](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-02)

**Critérios de aceitação:**
- [x] Integração com PKCS#11 via `SunPKCS11` provider do Java
- [x] Testes de integração utilizando SoftHSM2 como simulador de token/smart card (com `assumeTrue` para ambientes sem SoftHSM2)
- [x] Comportamento adequado quando dispositivo não está disponível (mensagem de erro clara, não crash)
- [x] Documentação do setup em `SOFTHSM2_SETUP.md`
- [x] CPF extraído do certificado no SoftHSM2 é usado em `who.identifier.value` do `Signature`, substituindo o valor fixo simulado

---

## Sprint 5 — CLI do simulador e download dinâmico (19/05 – 26/05)

**Objetivo:** Segunda aplicação CLI (`simulador`) com comandos `start`, `stop`, `status` e download automático do `simulador.jar`.

**Valor entregue:** US-03 completo. Usuário gerencia o ciclo de vida do Simulador do HubSaúde sem conhecer comandos Java.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-03.3 — Estrutura base do CLI `simulador`

> Épico: [US-03](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-03)

**Critérios de aceitação:**
- [ ] Projeto CLI `simulador` segue a mesma estrutura do CLI `assinatura`
- [ ] Comandos `start`, `stop` e `status` definidos
- [ ] Comando `simulador version` exibe a versão

#### US-03.4 — Download automático do simulador.jar e JRE

> Épico: [US-03](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-03)

**Critérios de aceitação:**
- [ ] CLI busca `release.json` via URL fixa (`https://raw.githubusercontent.com/{owner}/{repo}/main/release.json`) para identificar a versão mais recente do simulador.jar
- [ ] CLI compara a versão do `release.json` com a versão instalada localmente antes de baixar
- [ ] Download automático quando simulador.jar não está disponível localmente
- [ ] Versão já baixada não é baixada novamente (cache local em `~/.hubsaude/`)
- [ ] Opção `--source <url>` permite indicar URL alternativa para download
- [ ] Verificação de integridade do download (checksum SHA-256)
- [ ] CLI baixa o JRE automaticamente via Eclipse Temurin (Adoptium) caso não esteja disponível em `~/.hubsaude/`, usando as URLs por plataforma definidas no `release.json`

#### US-03.1 — Iniciar o Simulador via CLI

> Épico: [US-03](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-03)

**Critérios de aceitação:**
- [ ] Comando `simulador start` inicia o simulador.jar
- [ ] CLI verifica se as portas necessárias estão disponíveis antes de iniciar
- [ ] Se simulador.jar não está disponível localmente, é baixado automaticamente (US-03.4)
- [ ] Feedback exibido ao usuário sobre o status de inicialização

#### US-03.2 — Parar e monitorar o Simulador

> Épico: [US-03](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-03)

**Critérios de aceitação:**
- [ ] Comando `simulador stop` encerra o Simulador via endpoint `/shutdown`
- [ ] Comando `simulador status` exibe se o Simulador está em execução (via `/api/info`)
- [ ] Informações de processo (PID, porta) registradas em `~/.hubsaude/`
- [ ] Encerramento limpo do processo com tratamento adequado de erros

---

### @carlosmorais01 — assinador.jar (Java)

#### Documentação técnica e revisão de qualidade

**Critérios de aceitação:**
- [x] Javadoc nos métodos públicos principais
- [x] README com exemplos de uso via linha de comando e via HTTP
- [x] Guia de setup do SoftHSM2 (`SOFTHSM2_SETUP.md`)
- [x] Cobertura de testes adequada (unitários + integração)
- [x] Código limpo: sem TODOs pendentes, tratamento de exceções adequado

---

## Sprint 6 — CI/CD, releases e assinatura de artefatos (09/06 – 15/06)

**Objetivo:** Pipeline completo de entrega contínua. Build multiplataforma automático, publicação no GitHub Releases com versionamento SemVer, checksums SHA256 e assinatura via Cosign.

**Valor entregue:** US-05 completo. Todo push na main valida o build. Tags SemVer publicam releases com artefatos assinados e verificáveis.

> **Nota:** Esta sprint é **conjunta**.

---

### Tarefa conjunta

#### Revisão dos critérios de aceitação

> Referência: [criterios.md](https://github.com/kyriosdata/runner/blob/802d241630ab3eac231834bc6c8afdd948c56856/docs/criterios.md)

**Critérios de aceitação:**
- [x] Todos os itens de A a I revisados e pendências identificadas
- [x] Pendências do assinador.jar corrigidas (ADRs, cobertura JaCoCo, encoding UTF-8)
- [x] Pendências do CLI em Go corrigidas (assinatura.exe removido do versionamento, .gitignore atualizado)
- [x] Itens de CI obrigatório (lint + build + testes em Windows e Linux) cobertos pelo workflow

#### US-05.1 — GitHub Actions: build e testes contínuos

> Épico: [US-05](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-05)

**Critérios de aceitação:**
- [x] Workflow de CI executa a cada push na branch principal
- [x] Build do assinador.jar (`./mvnw verify`) executado e validado
- [x] Cross-compilation dos binários Go para `windows/amd64`, `linux/amd64` e `darwin/amd64`
- [x] Testes executados em Linux e Windows no CI

#### US-05.2 — Publicação automática de releases com SemVer

> Épico: [US-05](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-05)

**Critérios de aceitação:**
- [x] Tags de versão seguem SemVer (ex.: `v1.0.0`)
- [x] Workflow de release publicado automaticamente no GitHub Releases ao criar tag
- [x] Binários dos CLIs (`assinatura`) nomeados por plataforma (ex.: `assinatura-v1.0.0-linux-amd64`)
- [x] `assinador.jar` incluído na release

#### US-05.3 — Checksums SHA256 e assinatura com Cosign

> Épico: [US-05](https://github.com/kyriosdata/runner/blob/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c/contexto.md#us-05)

**Critérios de aceitação:**
- [x] Cada release inclui arquivo de checksums SHA256 para todos os artefatos
- [x] Artefatos assinados com Cosign (identidade OIDC + transparency log Sigstore)
- [x] Cada artefato acompanhado de `.sig` e `.pem`
- [x] Processo de assinatura automatizado no pipeline CI/CD
- [x] Documentação de verificação com `cosign verify-blob` presente no README

---

## Sprint 7 — Documentação, testes de aceitação e entrega final (15/06 – 22/06)

**Objetivo:** Sistema Runner completo, documentado, testado e com todos os critérios de aceitação verificados. Entrega da versão `v1.0.0`.

**Valor entregue:** Todos os entregáveis da especificação presentes e verificados.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### Manual de usuário dos CLIs

**Critérios de aceitação:**
- [ ] Guia de instalação para Windows, Linux e macOS
- [ ] Exemplos de uso para todos os comandos
- [ ] Seção de troubleshooting com erros comuns
- [ ] Instruções de verificação de integridade com Cosign

---

### @carlosmorais01 — assinador.jar (Java)

#### Testes de aceitação completos

**Critérios de aceitação:**
- [x] Cenários de teste baseados nos critérios de aceitação de US-01 a US-05
- [x] Cobertura de todos os cenários de erro documentados na spec SES-GO
- [x] Relatório de cobertura de testes gerado

---

### Tarefa conjunta

#### Revisão final e publicação da v1.0.0

**Critérios de aceitação:**
- [ ] Todos os critérios de aceitação de US-01 a US-05 verificados e marcados
- [ ] Sistema testado nas três plataformas (Windows, Linux, macOS)
- [ ] Tag `v1.0.0` criada e release publicada com todos os artefatos
- [ ] Documentação de uso incluída na release

---

## Resumo de Sprints

| Sprint | Período       | Foco                      | @V1N1C1U5ESPINDOLA (Go) | @carlosmorais01 (Java)     | Conjunto                  |
|--------|---------------|---------------------------|-------------------------|----------------------------|---------------------------|
| 0      | 14/04 – 21/04 | Estruturação base         | —                       | —                          | Setup do repositório      |
| 1      | 21/04 – 28/04 | Fundação                  | US-01.1, US-01.2        | US-02.1, US-02.2, US-02.3  | Contrato CLI ↔ jar        |
| 2      | 28/04 – 05/05 | Fluxo local ponta-a-ponta | US-01.3, US-01.4        | Testes de integração       | —                         |
| 3      | 05/05 – 12/05 | JDK + Modo HTTP           | US-04.1                 | US-02.4 (HTTP)             | —                         |
| 4      | 12/05 – 19/05 | Modo servidor             | US-01.5–US-01.9         | US-02.5 (PKCS#11/SoftHSM2) | —                         |
| 5      | 19/05 – 26/05 | CLI simulador             | US-03.1–US-03.4         | Docs + qualidade           | —                         |
| 6      | 09/06 – 15/06 | CI/CD e releases          | —                       | —                          | US-05.1, US-05.2, US-05.3 |
| 7      | 15/06 – 22/06 | Entrega final             | Docs de usuário         | Testes de aceitação        | Revisão + v1.0.0          |