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

Pré-requisitos: [Go 1.22+](https://go.dev/dl/) e Java 17+.

```bash
# Clonar o repositório
git clone https://github.com/SEU_ORG/runner.git
cd runner

# Compilar o CLI
cd assinatura && make build

# Assinar um documento
./assinatura assinar --bundle bundle.json --provenance provenance.json --pin 1234

# Validar um documento
./assinatura validar --signature signature.json --bundle bundle.json
```

Consulte o README de cada componente para instruções detalhadas.

## Documentação

- [`assinatura/README.md`](./assinatura/README.md) — como compilar e usar o CLI
- [`docs/CONTRATO.md`](./docs/CONTRATO.md) — contrato de comunicação entre CLI e JAR
- [`docs/adr/`](./docs/adr/) — decisões de arquitetura registradas

## Especificação

Este projeto implementa a especificação disponível em [kyriosdata/runner](https://github.com/kyriosdata/runner/tree/COMMIT_OU_TAG_AQUI). Em caso de divergência, a especificação prevalece.

## Contribuindo

1. Abra uma issue descrevendo a mudança proposta
2. Faça um fork e crie um branch a partir de `main`
3. Submeta um PR vinculado à issue — o CI precisa estar verde para merge

## Equipe

Projeto desenvolvido por Carlos Henrique Alves e Vinícius Espíndola sob orientação do prof. Fábio Nogueira de Lucena (UFG).