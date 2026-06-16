# ADR-001 — Modo dual de invocação do assinador.jar

**Data:** 2026-04-14  
**Status:** Aceito

## Contexto

O CLI (`assinatura`, em Go) precisa se comunicar com o `assinador.jar` para criar e validar assinaturas digitais. Dois mecanismos são possíveis: subprocess (`java -jar`) e HTTP. Cada um tem trade-offs distintos de latência e complexidade.

## Decisão

Suportar **dois modos de invocação** no CLI:

- **Modo local (subprocess):** CLI executa `java -jar assinador.jar` a cada chamada. Adequado para uso esporádico.
- **Modo servidor (HTTP, padrão):** `assinador.jar` fica em execução permanente; CLI envia requisições HTTP para `/sign` e `/validate`. É o modo padrão por eliminar o overhead de cold start da JVM em chamadas repetidas.

O CLI detecta automaticamente se há uma instância do servidor ativa (via health check real, não apenas "porta ocupada") e a reutiliza. A flag `--local` força o modo subprocess explicitamente.

## Consequências

- **Positivas:** menor latência para cenários com múltiplas chamadas; o usuário não precisa gerenciar o ciclo de vida do servidor manualmente no caso comum.
- **Negativas:** a detecção de instância ativa adiciona complexidade ao CLI (leitura de PID em `~/.hubsaude/assinador.pid` + health check HTTP).
- O `assinador.jar` precisa expor tanto a interface CLI (PicoCLI) quanto a interface HTTP (Spring Boot), mantendo a mesma lógica de validação em ambos.
