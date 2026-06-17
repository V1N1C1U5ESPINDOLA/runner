# ADR-002 — SoftHSM2 como simulador de dispositivo PKCS#11

**Data:** 2026-05-12  
**Status:** Aceito

## Contexto

A especificação exige que o `assinador.jar` suporte dispositivos criptográficos reais (token USB / smart card) via interface PKCS#11 para extração do CPF do signatário e assinatura. No ambiente de desenvolvimento e CI não há dispositivos físicos disponíveis. É necessário um simulador.

## Decisão

Usar **SoftHSM2** como simulador de dispositivo PKCS#11 durante o desenvolvimento e nos testes de integração.

- O `assinador.jar` usa o provider `SunPKCS11` da JVM padrão, apontando para a biblioteca do SoftHSM2 via `pkcs11.cfg`.
- Testes de integração que dependem de SoftHSM2 usam `assumeTrue(softhsm2Disponivel())` para serem ignorados em ambientes sem o software instalado, em vez de falharem.
- O fluxo real com token físico não é testado no CI (fora do escopo desta implementação — assinatura criptográfica real está fora do escopo conforme a especificação).

## Consequências

- **Positivas:** permite validar a integração PKCS#11 sem hardware; setup documentado em `SOFTHSM2_SETUP.md`; CI não falha em ambientes sem SoftHSM2.
- **Negativas:** o comportamento com tokens físicos reais não é coberto pelos testes automatizados; SoftHSM2 precisa ser instalado manualmente no ambiente de desenvolvimento.
