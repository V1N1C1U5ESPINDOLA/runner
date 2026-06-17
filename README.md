# Runner

Sistema para assinatura e validação de documentos FHIR digitais, desenvolvido como projeto da disciplina de Implementação e Integração de Software — Bacharelado em Engenharia de Software, UFG.

> **Status:** em desenvolvimento ativo. Funcionalidades e interfaces podem mudar a qualquer momento.

## O que é

O Runner é um conjunto de ferramentas que permite assinar e validar documentos clínicos no padrão [FHIR](https://hl7.org/fhir/), conforme exigido pelo ecossistema da SES-GO. O projeto é uma implementação sobre a especificação definida em [kyriosdata/runner](https://github.com/kyriosdata/runner).

## Componentes

| Componente | Descrição |
|---|---|
| `assinatura/` | CLI em Go — interface de linha de comando para assinar e validar |
| `assinador.jar` | Biblioteca Java responsável pela criptografia e lógica de assinatura |

## Início rápido

Pré-requisitos: [Go 1.22+](https://go.dev/dl/) e [Java 21+](https://adoptium.net/).

```bash
# Clonar o repositório
git clone https://github.com/V1N1C1U5ESPINDOLA/runner.git
cd runner

# Compilar o CLI
cd assinatura && make build

# Assinar um documento
./assinatura assinar --bundle bundle.json --provenance provenance.json --pin 1234

# Validar um documento
./assinatura validar --signature signature.json --bundle bundle.json
```

## Como executar os testes

```bash
# Java — roda testes unitários, de integração e gera relatório JaCoCo
cd assinador-digital && ./mvnw verify

# Go — roda todos os testes do CLI
cd assinatura && go test ./...
```

Consulte o README de cada componente para instruções detalhadas.

## Documentação

- [`assinatura/README.md`](./assinatura/README.md) — como compilar e usar o CLI
- [`assinador-digital/docs/CONTRATO.md`](./assinador-digital/docs/CONTRATO.md) — contrato de comunicação entre CLI e JAR
- [`docs/adr/`](./docs/adr/) — decisões de arquitetura registradas

## Verificando a integridade dos artefatos

Cada release publica checksums SHA256 e assinaturas Cosign (`.sig` + `.pem`) para todos os artefatos. Para verificar:

```bash
# Instalar cosign: https://docs.sigstore.dev/cosign/system_config/installation/

cosign verify-blob \
  --certificate assinatura-v1.0.0-linux-amd64.pem \
  --signature   assinatura-v1.0.0-linux-amd64.sig \
  assinatura-v1.0.0-linux-amd64
```

O mesmo vale para o `assinador.jar`:

```bash
cosign verify-blob \
  --certificate assinador-v1.0.0.jar.pem \
  --signature   assinador-v1.0.0.jar.sig \
  assinador-v1.0.0.jar
```

## Especificação

Este projeto implementa a especificação disponível em [kyriosdata/runner @ 4d7d40f](https://github.com/kyriosdata/runner/tree/4d7d40fff32b3b50372e7fbe41fe713b2bbddb4c). Em caso de divergência, a especificação prevalece.

## Contribuindo

1. Abra uma issue descrevendo a mudança proposta
2. Faça um fork e crie um branch a partir de `main`
3. Submeta um PR vinculado à issue — o CI precisa estar verde para merge

## Equipe

Projeto desenvolvido por Carlos Henrique Alves e Vinícius Espíndola sob orientação do prof. Fábio Nogueira de Lucena (UFG).