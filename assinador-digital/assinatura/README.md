# assinatura — CLI do Sistema Runner

CLI em Go para criação e validação de assinaturas digitais no padrão FHIR da SES-GO.
Faz parte do Sistema Runner (disciplina Implementação e Integração, 2026-01).

## Estrutura de pacotes

```
assinatura/
├── main.go                        # Ponto de entrada
├── go.mod                         # Módulo Go e dependências
├── Makefile                       # Atalhos de build e teste
└── internal/
    └── runner/
        ├── root.go                # Comando raiz (assinatura)
        ├── version.go             # Subcomando: assinatura version
        ├── assinar.go             # Subcomando: assinatura assinar
        ├── validar.go             # Subcomando: assinatura validar
        ├── validacoes.go          # Funções de validação compartilhadas
        └── validacoes_test.go     # Testes de parsing e validação
```

## Pré-requisitos

- [Go 1.22+](https://go.dev/dl/)

## Como compilar

```bash
# Compilar para a plataforma atual
go build -o assinatura .

# Ou usando o Makefile
make build
```

## Como usar

```bash
# Ver a versão
assinatura version

# Criar uma assinatura digital
assinatura assinar --bundle bundle.json --provenance provenance.json --pin 1234

# Validar uma assinatura digital
assinatura validar --signature signature.json --bundle bundle.json

# Ver ajuda geral
assinatura --help

# Ver ajuda de um subcomando
assinatura assinar --help
assinatura validar --help
```

## Como executar os testes

```bash
go test ./...

# Ou usando o Makefile
make test
```

## Compilar para todas as plataformas

```bash
make build-all
# Gera binários em dist/ para Linux, Windows e macOS
```

## Contrato com assinador.jar

A comunicação entre este CLI e o `assinador.jar` está definida em [`CONTRATO.md`](../CONTRATO.md) na raiz do repositório.
