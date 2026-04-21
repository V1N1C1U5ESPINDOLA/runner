# Plano de Trabalho — Sistema Runner

## Premissas

- **CLI (assinatura e simulador):** Go, desenvolvido pelo @V1N1C1U5ESPINDOLA.
- **assinador.jar:** Java 21 + Spring Boot + PicoCLI, desenvolvido pelo @carlosmorais01.
- **Estratégia:** iterativa e incremental, organizada em **7 sprints de 1 semana** (terça a terça).
- **Divisão:** @V1N1C1U5ESPINDOLA foca nos dois CLIs em Go; @carlosmorais01 foca no assinador.jar. Tarefas de CI/CD e integração são conjuntas.
- As histórias abaixo são subdivisões dos épicos US-01 a US-05 da especificação.

---

## Rastreabilidade Épicos → Histórias

| Épico | Descrição                                | Histórias derivadas                                                             |
|-------|------------------------------------------|---------------------------------------------------------------------------------|
| US-01 | Invocar assinador.jar via CLI            | US-01.1, US-01.2, US-01.3, US-01.4, US-01.5, US-01.6, US-01.7, US-01.8, US-01.9 |
| US-02 | Simular assinatura digital com validação | US-02.1, US-02.2, US-02.3, US-02.4, US-02.5                                     |
| US-03 | Gerenciar Ciclo de Vida do Simulador     | US-03.1, US-03.2, US-03.3, US-03.4                                              |
| US-04 | Provisionar JDK Automaticamente          | US-04.1                                                                         |
| US-05 | Disponibilizar binários multiplataforma  | US-05.1, US-05.2, US-05.3                                                       |

---

## Sprint 0 — Estruturação Base (07/04 – 14/04)
**Objetivo:** Consolidar o protótipo já desenvolvido anteriormente, documentar o que foi feito e alinhar a equipe sobre o estado atual do código antes de iniciar o desenvolvimento incremental. Valor entregue: Repositório organizado com o protótipo registrado, decisões de arquitetura documentadas e backlog priorizado para a Sprint 1.
**Tarefa conjunta**
- [ ] Código do protótipo commitado e organizado no repositório
- [ ] README inicial descrevendo o estado atual do projeto
- [ ] Decisões de arquitetura registradas (estrutura de pacotes Go, estrutura Spring Boot)
- [ ] Backlog revisado e critérios de aceitação confirmados para Sprint 1
- [ ] Plano de trabalho configurado e organizado


## Sprint 1 — Fundação dos projetos (21/04 – 28/04)

**Objetivo:** Estrutura base do CLI em Go funcionando. Consolidar e completar o que já existe no assinador.jar — especialmente a validação de parâmetros e o mock da resposta no formato FHIR correto.

**Valor entregue:** Os dois projetos compilam, rodam e têm os contratos de entrada/saída bem definidos entre si.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-01.1 — Estrutura base do CLI (Go)

**Como** usuário do Sistema Runner,  
**quero** que o projeto CLI esteja estruturado com organização de pacotes e build funcional,  
**para que** o desenvolvimento possa progredir de forma organizada.

**Critérios de aceitação:**
- [ ] Projeto Go inicializado com `go mod init`
- [ ] Cobra CLI instalado (`go install github.com/spf13/cobra-cli@latest`)
- [ ] Estrutura de pacotes definida e documentada
- [ ] Comando `assinatura version` exibe a versão atual do CLI
- [ ] Aplicação compila e executa nas três plataformas (Windows, Linux, macOS)

#### US-01.2 — Parsing de comandos e parâmetros no CLI

**Como** usuário do Sistema Runner,  
**quero** executar comandos `assinar` e `validar` com parâmetros via linha de comandos,  
**para que** eu possa solicitar operações de assinatura de forma intuitiva.

**Critérios de aceitação:**
- [ ] CLI aceita o comando `assinar` com os parâmetros: `--pin`, `--bundle`, `--provenance`
- [ ] CLI aceita o comando `validar` com os parâmetros: `--signature`, `--bundle`
- [ ] Mensagem de ajuda (`--help`) documenta os comandos e parâmetros disponíveis
- [ ] Parâmetros ausentes ou inválidos geram mensagem de erro orientativa
- [ ] Testes cobrem o parsing de comandos e parâmetros

---

### @carlosmorais01 — assinador.jar (Java)

> **Nota:** O projeto já possui estrutura Spring Boot + PicoCLI, subcomandos `assinar`/`validar`, validação de PIN e janela de tempo. As tarefas abaixo completam e corrigem o que falta.

#### US-02.1 — Mock da resposta de assinatura no formato FHIR correto

**Como** usuário do Sistema Runner,  
**quero** que o assinador.jar retorne uma resposta simulada que esteja em conformidade com a especificação FHIR da SES-GO,  
**para que** o CLI possa exibir uma resposta estruturalmente válida mesmo em modo simulado.

> **Contexto:** A resposta atual em `gerarMockSignature()` está incompleta. A spec exige os campos abaixo.

**Campos obrigatórios no `Signature` retornado (conforme spec SES-GO):**
- `resourceType`: `"Signature"`
- `type`: array com `system: "urn:iso-astm:E1762-95:2013"` e `code` (ex.: `1.2.840.10065.1.12.1.1`)
- `when`: timestamp ISO-8601 do momento da assinatura
- `who`: objeto com `identifier.system` (`urn:brasil:cpf` ou `urn:brasil:cnpj`) e `identifier.value` (CPF/CNPJ simulado)
- `sigFormat`: `"application/jose"`
- `targetFormat`: `"application/octet-stream"`
- `data`: string base64 contendo um JWS JSON Serialization simulado (estrutura com `payload`, `signatures[0].protected`, `signatures[0].header`, `signatures[0].signature`)

**Critérios de aceitação:**
- [ ] `gerarMockSignature()` retorna todos os campos obrigatórios acima
- [ ] O campo `data` contém um JWS JSON Serialization em base64 válido (estrutura correta, valores simulados)
- [ ] Testes unitários verificam a presença e formato de todos os campos obrigatórios

#### US-02.2 — Validação completa de parâmetros de criação de assinatura

**Como** usuário do Sistema Runner,  
**quero** que o assinador.jar valide rigorosamente todos os parâmetros de entrada antes de simular a assinatura,  
**para que** eu receba feedback imediato e claro sobre qualquer erro.

> **Contexto:** Validações de PIN e janela de tempo já existem. As abaixo completam a cobertura conforme a spec.

**Critérios de aceitação:**
- [ ] Arquivo Bundle existe e é um JSON válido
- [ ] Arquivo Provenance existe, é um JSON válido e contém o campo `recorded`
- [ ] `Provenance.recorded` está em formato ISO-8601 válido
- [ ] Timestamp de `Provenance.recorded` está dentro da janela de ±5 minutos (já implementado — garantir cobertura de teste)
- [ ] PIN contém apenas dígitos e tem entre 4 e 8 caracteres (já implementado — garantir cobertura de teste)
- [ ] Mensagens de erro indicam qual parâmetro está inválido e o motivo
- [ ] Testes unitários cobrem todos os cenários de validação (sucesso e falha)

#### US-02.3 — Simulação e validação de parâmetros de validação de assinatura

**Como** usuário do Sistema Runner,  
**quero** que o assinador.jar valide os parâmetros do comando `validar` e retorne um resultado simulado,  
**para que** eu possa testar o fluxo de validação com feedback claro.

> **Contexto:** O método `validarAssinatura()` tem um TODO. A simulação pode ser simples: se os arquivos existem e o JSON do Signature tem a estrutura mínima esperada, retorna válido.

**Critérios de aceitação:**
- [ ] Arquivo Signature existe e é um JSON com `resourceType: "Signature"` e campo `data` presente
- [ ] Arquivo Bundle original existe e é um JSON válido
- [ ] Resultado simulado retorna `true` (válido) para entradas bem formadas
- [ ] Resultado retorna `false` (inválido) quando o Signature está malformado ou o Bundle ausente
- [ ] Mensagens de erro claras para parâmetros inválidos
- [ ] Testes unitários cobrem cenários de sucesso e falha

---

### Tarefa conjunta

#### Contrato CLI ↔ assinador.jar

Definir juntos — antes de iniciar a Sprint 2 — o contrato exato de comunicação:

- Argumentos de linha de comando que o CLI passa ao `java -jar assinador.jar` (ordem, nomes de flags)
- Formato da saída em stdout para sucesso (JSON do `Signature`)
- Formato da saída em stderr para erros (mensagem legível ou JSON de `OperationOutcome`)
- Código de saída: `0` para sucesso, `1` para erro

Registrar isso como comentário no topo dos arquivos de integração ou em um `CONTRATO.md` no repositório.

---

## Sprint 2 — Fluxo ponta-a-ponta modo local (28/04 – 05/05)

**Objetivo:** O usuário consegue executar `assinatura assinar ...` e obter uma assinatura FHIR simulada, sem precisar digitar nenhum comando Java manualmente.

**Valor entregue:** Caso de uso principal funcionando de ponta a ponta.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-01.3 — Invocação local do assinador.jar

**Como** usuário do Sistema Runner,  
**quero** que o CLI invoque o assinador.jar diretamente via `java -jar` com os parâmetros fornecidos,  
**para que** eu possa assinar e validar sem executar comandos Java manualmente.

**Critérios de aceitação:**
- [ ] CLI localiza o `java` disponível no PATH (ou provisionado — ver Sprint 3)
- [ ] CLI constrói e executa `java -jar assinador.jar` com os parâmetros corretamente mapeados
- [ ] Saída stdout do assinador.jar é capturada e repassada ao usuário
- [ ] Erros (JDK ausente, jar não encontrado, código de saída != 0) são tratados com mensagens claras
- [ ] Testes de integração validam o fluxo CLI → assinador.jar

#### US-01.4 — Exibição legível de resultados

**Como** usuário do Sistema Runner,  
**quero** que o CLI apresente o resultado das operações de forma clara,  
**para que** eu compreenda facilmente o resultado da assinatura ou validação.

**Critérios de aceitação:**
- [ ] Resultado de criação de assinatura é formatado de forma legível (JSON indentado ou resumo)
- [ ] Resultado de validação indica claramente se a assinatura é válida ou inválida
- [ ] Erros do assinador.jar são apresentados de forma clara ao usuário

---

### @carlosmorais01 — assinador.jar (Java)

#### Revisão e testes de integração do assinador.jar

**Critérios de aceitação:**
- [ ] Fluxo completo `assinar` funciona corretamente via `java -jar` com os argumentos definidos no contrato
- [ ] Fluxo completo `validar` funciona corretamente via `java -jar`
- [ ] Testes de integração cobrindo o fluxo completo (assinar e validar) via invocação de linha de comando

---

## Sprint 3 — Provisionamento de JDK (05/05 – 12/05)

**Objetivo:** O CLI detecta, baixa e configura o JDK automaticamente quando ausente. O usuário sem Java instalado consegue usar o sistema normalmente.

**Valor entregue:** US-04 completo. Elimina pré-requisito de instalação manual do Java.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-04.1 — Detecção e download automático do JDK

**Como** usuário do Sistema Runner,  
**quero** que o sistema baixe e configure automaticamente o JDK necessário quando não estiver disponível,  
**para que** eu possa usar o Assinador sem instalar ou configurar o Java manualmente.

**Critérios de aceitação:**
- [ ] CLI detecta se o JDK está presente na versão exigida (verificação do PATH e/ou diretório local `~/.hubsaude/jdk/`)
- [ ] CLI baixa o JDK compatível para a plataforma corrente (Windows/Linux/macOS, amd64) quando ausente
- [ ] JDK baixado é armazenado em `~/.hubsaude/jdk/` e usado nas invocações do assinador.jar
- [ ] Download funciona nas três plataformas
- [ ] Feedback exibido ao usuário durante o download (progresso ou mensagem de status)

---

### @carlosmorais01 — assinador.jar (Java)

#### US-02.4 — Endpoints HTTP do assinador.jar (modo servidor)

**Como** usuário do Sistema Runner,  
**quero** que o assinador.jar exponha endpoints HTTP `/sign` e `/validate`,  
**para que** o CLI possa invocá-lo via requisições HTTP no modo servidor (menor latência).

**Critérios de aceitação:**
- [ ] `SignatureController` implementado com `POST /sign` e `POST /validate`
- [ ] Endpoints reutilizam a mesma lógica de validação e simulação já existente
- [ ] Respostas HTTP seguem estrutura consistente para sucesso (200 + JSON do Signature) e erro (4xx + mensagem clara)
- [ ] Testes de integração validam os endpoints HTTP

---

## Sprint 4 — Modo servidor e gerenciamento de ciclo de vida (12/05 – 19/05)

**Objetivo:** CLI inicia, detecta, usa e encerra o assinador.jar no modo servidor HTTP. O modo servidor passa a ser o padrão.

**Valor entregue:** Menor latência disponível (warm start). CLI gerencia o ciclo de vida completo do assinador.jar.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-01.5 — Iniciar assinador.jar no modo servidor

**Como** usuário do Sistema Runner,  
**quero** que o CLI inicie o assinador.jar como servidor HTTP em background,  
**para que** ele fique disponível para requisições com menor latência.

**Critérios de aceitação:**
- [ ] CLI inicia o assinador.jar como processo em background na porta padrão
- [ ] PID e porta do processo são registrados em `~/.hubsaude/assinador.pid`
- [ ] Feedback exibido ao usuário confirmando que o servidor iniciou
- [ ] Porta pode ser personalizada via parâmetro `--port`

#### US-01.6 — Invocar assinador.jar via HTTP

**Como** usuário do Sistema Runner,  
**quero** que o CLI envie requisições HTTP ao assinador.jar quando em modo servidor,  
**para que** eu tenha menor latência nas operações.

**Critérios de aceitação:**
- [ ] CLI envia requisições HTTP para `/sign` e `/validate`
- [ ] Modo servidor é utilizado por padrão quando há instância em execução
- [ ] Flag `--local` força uso do modo local (invocação direta)
- [ ] Testes de integração validam o fluxo CLI → HTTP → assinador.jar

#### US-01.7 — Detectar instância do assinador.jar em execução

**Como** usuário do Sistema Runner,  
**quero** que o CLI detecte se já existe uma instância do assinador.jar em execução,  
**para que** não sejam criadas instâncias duplicadas desnecessariamente.

**Critérios de aceitação:**
- [ ] CLI consulta `~/.hubsaude/assinador.pid` para verificar processo registrado
- [ ] Health check HTTP confirma que o processo está respondendo
- [ ] Se instância ativa encontrada, CLI a reutiliza em vez de iniciar nova
- [ ] Se processo registrado não responde, é considerado inativo

#### US-01.8 — Interromper execução do assinador.jar

**Como** usuário do Sistema Runner,  
**quero** interromper a execução do assinador.jar,  
**para que** eu tenha controle sobre os processos em execução no meu sistema.

**Critérios de aceitação:**
- [ ] Comando `assinatura stop` encerra o assinador.jar na porta padrão
- [ ] Parâmetro `--port` permite especificar a porta do processo a encerrar
- [ ] Feedback exibido confirmando o encerramento
- [ ] Registro em `~/.hubsaude/` é atualizado após encerramento

#### US-01.9 — Agendar interrupção por inatividade

**Como** usuário do Sistema Runner,  
**quero** agendar a interrupção automática do assinador.jar após um período sem interação,  
**para que** recursos do sistema sejam liberados automaticamente.

**Critérios de aceitação:**
- [ ] Parâmetro `--timeout <minutos>` define tempo máximo de inatividade ao iniciar o servidor
- [ ] Após o período sem requisições, assinador.jar é encerrado automaticamente
- [ ] Mecanismo de timeout documentado no `--help` do CLI

---

### @carlosmorais01 — assinador.jar (Java)

#### US-02.5 — Integração com dispositivo criptográfico via PKCS#11 (SoftHSM2)

**Como** usuário do Sistema Runner,  
**quero** que o assinador.jar suporte interação com dispositivo criptográfico (token/smart card) via PKCS#11,  
**para que** eu possa usar material criptográfico real ou simulado nas operações de assinatura.

> **Biblioteca recomendada pelo professor:** SoftHSM2 como simulador de hardware criptográfico, acessado via `SunPKCS11` provider do Java.

**Critérios de aceitação:**
- [ ] Integração com PKCS#11 via `SunPKCS11` provider do Java
- [ ] Testes de integração utilizando **SoftHSM2** como simulador de token/smart card
- [ ] Comportamento adequado quando dispositivo não está disponível (mensagem de erro clara, não crash)
- [ ] Documentação do setup necessário para uso com SoftHSM2 (instalação, configuração do slot)

---

## Sprint 5 — CLI do simulador e download dinâmico (19/05 – 26/05)

**Objetivo:** Segunda aplicação CLI (`simulador`) com comandos `start`, `stop`, `status` e download automático do `simulador.jar`.

**Valor entregue:** US-03 completo. Usuário gerencia o ciclo de vida do Simulador do HubSaúde sem conhecer comandos Java.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### US-03.3 — Estrutura base do CLI `simulador`

**Como** usuário do Sistema Runner,  
**quero** um CLI dedicado para o Simulador com estrutura própria,  
**para que** a gestão do Simulador tenha interface independente e clara.

**Critérios de aceitação:**
- [ ] Projeto CLI `simulador` segue a mesma estrutura do CLI `assinatura`
- [ ] Comandos `start`, `stop` e `status` definidos
- [ ] Comando `simulador version` exibe a versão

#### US-03.4 — Download automático do simulador.jar

**Como** usuário do Sistema Runner,  
**quero** que o CLI baixe automaticamente a versão mais recente do simulador.jar do GitHub Releases,  
**para que** eu sempre utilize a versão atualizada sem download manual.

**Critérios de aceitação:**
- [ ] CLI consulta GitHub Releases para identificar a versão mais recente do simulador.jar
- [ ] Download automático quando simulador.jar não está disponível localmente
- [ ] Versão já baixada não é baixada novamente (cache local em `~/.hubsaude/`)
- [ ] Opção `--source <url>` permite indicar URL alternativa para download
- [ ] Verificação de integridade do download (checksum SHA-256)

#### US-03.1 — Iniciar o Simulador via CLI

**Como** usuário do Sistema Runner,  
**quero** iniciar o Simulador do HubSaúde através do CLI,  
**para que** eu possa gerenciá-lo sem conhecer os comandos Java subjacentes.

**Critérios de aceitação:**
- [ ] Comando `simulador start` inicia o simulador.jar
- [ ] CLI verifica se as portas necessárias estão disponíveis antes de iniciar
- [ ] Se simulador.jar não está disponível localmente, é baixado automaticamente (US-03.4)
- [ ] Feedback exibido ao usuário sobre o status de inicialização

#### US-03.2 — Parar e monitorar o Simulador

**Como** usuário do Sistema Runner,  
**quero** parar o Simulador e consultar seu status atual,  
**para que** eu tenha visibilidade e controle sobre seu ciclo de vida.

**Critérios de aceitação:**
- [ ] Comando `simulador stop` encerra o Simulador
- [ ] Comando `simulador status` exibe se o Simulador está em execução ou não
- [ ] Informações de processo (PID, porta) registradas em `~/.hubsaude/`
- [ ] Encerramento limpo do processo com tratamento adequado de erros

---

### @carlosmorais01 — assinador.jar (Java)

#### Documentação técnica e revisão de qualidade

**Critérios de aceitação:**
- [ ] Javadoc nos métodos públicos principais
- [ ] README com exemplos de uso via linha de comando e via HTTP
- [ ] Guia de setup do SoftHSM2 para desenvolvimento e testes
- [ ] Cobertura de testes adequada (unitários + integração)
- [ ] Código limpo: sem TODOs pendentes, tratamento de exceções adequado

---

## Sprint 6 — CI/CD, releases e assinatura de artefatos (26/05 – 02/06)

**Objetivo:** Pipeline completo de entrega contínua. Build multiplataforma automático, publicação no GitHub Releases com versionamento SemVer, checksums SHA256 e assinatura via Cosign.

**Valor entregue:** US-05 completo. Todo push na main gera binários. Tags SemVer publicam releases com artefatos assinados e verificáveis.

> **Nota:** Esta sprint é **conjunta**. Ambos trabalham juntos na configuração do pipeline, pois envolve decisões de repositório, secrets do GitHub Actions e integração dos dois CLIs numa mesma release.

---

### Tarefa conjunta

#### US-05.1 — GitHub Actions: build multiplataforma

**Como** desenvolvedor do Sistema Runner,  
**quero** que alterações no repositório disparem automaticamente a compilação para Windows, Linux e macOS,  
**para que** binários atualizados estejam sempre disponíveis após cada mudança.

**Critérios de aceitação:**
- [ ] GitHub Actions configurado com workflow de build
- [ ] Cross-compilation para `windows/amd64`, `linux/amd64` e `darwin/amd64`
- [ ] Build executado a cada push na branch principal
- [ ] Artefatos de build disponíveis como artifacts do workflow

#### US-05.2 — Publicação automática de releases com SemVer

**Como** usuário do Sistema Runner,  
**quero** baixar binários pré-compilados para minha plataforma a partir do GitHub Releases,  
**para que** eu possa utilizar o sistema sem necessidade de compilação.

**Critérios de aceitação:**
- [ ] Tags de versão seguem SemVer (ex.: `v1.0.0`)
- [ ] Workflow de release gera binários nomeados por plataforma (ex.: `assinatura-1.0.0-linux-amd64.AppImage`)
- [ ] Binários de ambos os CLIs (`assinatura` e `simulador`) publicados automaticamente no GitHub Releases ao criar tag
- [ ] assinador.jar incluído na release

#### US-05.3 — Checksums SHA256 e assinatura com Cosign

**Como** usuário do Sistema Runner,  
**quero** que os binários distribuídos incluam checksums SHA256 e assinatura via Cosign,  
**para que** eu possa verificar a integridade e autenticidade dos artefatos baixados.

**Critérios de aceitação:**
- [ ] Cada release inclui arquivo de checksums SHA256 para todos os binários
- [ ] Artefatos assinados com Cosign (identidade OIDC + transparency log Sigstore)
- [ ] Cada artefato acompanhado de `.sig` e `.pem` (ex.: `assinatura-1.0.0-linux-amd64.AppImage.sig`)
- [ ] Processo de assinatura automatizado no pipeline CI/CD
- [ ] Documentação de como verificar artefatos com `cosign verify-blob`

---

## Sprint 7 — Documentação, testes de aceitação e entrega final (02/06 – 09/06)

**Objetivo:** Sistema Runner completo, documentado, testado e com todos os critérios de aceitação da especificação verificados. Entrega da versão `v1.0.0`.

**Valor entregue:** Todos os entregáveis do item 7 da especificação presentes e verificados.

---

### @V1N1C1U5ESPINDOLA — CLI em Go

#### Manual de usuário dos CLIs

**Critérios de aceitação:**
- [ ] Guia de instalação para Windows, Linux e macOS
- [ ] Exemplos de uso para todos os comandos (`assinatura assinar`, `assinatura validar`, `assinatura stop`, `simulador start`, etc.)
- [ ] Seção de troubleshooting com erros comuns
- [ ] Instruções de verificação de integridade com Cosign

---

### @carlosmorais01 — assinador.jar (Java)

#### Testes de aceitação completos

**Critérios de aceitação:**
- [ ] Cenários de teste baseados nos critérios de aceitação de US-01 a US-05
- [ ] Cobertura de todos os cenários de erro documentados na spec SES-GO
- [ ] Relatório de cobertura de testes gerado

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

| Sprint | Período       | Foco                      | @V1N1C1U5ESPINDOLA (Go) | @carlosmorais01 (Java)                | Conjunto                  |
|--------|---------------|---------------------------|-------------------------|---------------------------------------|---------------------------|
| 0      | 14/04 - 21/04 | Estruturação base         | —                       | —                                     | —                         |
| 1      | 21/04 – 28/04 | Fundação                  | US-01.1, US-01.2        | US-02.1 (mock FHIR), US-02.2, US-02.3 | Contrato CLI ↔ jar        |
| 2      | 28/04 – 05/05 | Fluxo local ponta-a-ponta | US-01.3, US-01.4        | Testes de integração                  | —                         |
| 3      | 05/05 – 12/05 | JDK + Modo HTTP           | US-04.1                 | US-02.4 (HTTP)                        | —                         |
| 4      | 12/05 – 19/05 | Modo servidor             | US-01.5–US-01.9         | US-02.5 (PKCS#11/SoftHSM2)            | —                         |
| 5      | 19/05 – 26/05 | CLI simulador             | US-03.1–US-03.4         | Docs + qualidade                      | —                         |
| 6      | 26/05 – 02/06 | CI/CD e releases          | —                       | —                                     | US-05.1, US-05.2, US-05.3 |
| 7      | 02/06 – 09/06 | Entrega final             | Docs de usuário         | Testes de aceitação                   | Revisão + v1.0.0          |