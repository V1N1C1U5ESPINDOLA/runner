# Makefile — CLI assinatura (Sistema Runner)

BINARY   := assinatura
VERSION  := 0.1.0
LDFLAGS  := -ldflags "-X github.com/V1N1C1U5ESPINDOLA/runner/assinatura/internal/runner.Version=$(VERSION)"

## build: compila para a plataforma atual
build:
	go build $(LDFLAGS) -o $(BINARY) .

## test: executa todos os testes
test:
	go test ./...

## build-all: compila para Windows, Linux e macOS (amd64)
build-all:
	GOOS=linux   GOARCH=amd64 go build $(LDFLAGS) -o dist/$(BINARY)-$(VERSION)-linux-amd64   .
	GOOS=windows GOARCH=amd64 go build $(LDFLAGS) -o dist/$(BINARY)-$(VERSION)-windows-amd64.exe .
	GOOS=darwin  GOARCH=amd64 go build $(LDFLAGS) -o dist/$(BINARY)-$(VERSION)-darwin-amd64  .

## clean: remove binários gerados
clean:
	rm -f $(BINARY)
	rm -rf dist/

.PHONY: build test build-all clean
