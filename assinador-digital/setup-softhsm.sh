#!/bin/bash
set -e

MODULE=/usr/lib/softhsm/libsofthsm2.so
SLOT_LABEL="MeuToken"
PIN="1234"
SO_PIN="123456"
KEY_LABEL="minha-chave"
KEY_ID="01"

# Configura SoftHSM2 para o usuário atual se não estiver configurado
SOFTHSM2_CONF_DIR="$HOME/.config/softhsm2"
SOFTHSM2_TOKEN_DIR="$HOME/.local/share/softhsm2/tokens"
export SOFTHSM2_CONF="$SOFTHSM2_CONF_DIR/softhsm2.conf"

if [ ! -f "$SOFTHSM2_CONF" ]; then
  echo "=== Criando configuração do SoftHSM2 para o usuário $USER ==="
  mkdir -p "$SOFTHSM2_CONF_DIR"
  mkdir -p "$SOFTHSM2_TOKEN_DIR"
  cat > "$SOFTHSM2_CONF" << EOF
directories.tokendir = $SOFTHSM2_TOKEN_DIR/
objectstore.backend = file
log.level = ERROR
slots.removable = false
EOF
  echo "Configuração criada em $SOFTHSM2_CONF"
fi

SLOT_ID=$(pkcs11-tool --module $MODULE -L 2>/dev/null \
  | grep -B1 "token label.*$SLOT_LABEL" \
  | grep "Slot" | awk '{print $NF}' | head -1)

echo "Slot ID: $SLOT_ID"

# VERIFICA SE CHAVE JÁ EXISTE
CHAVE=$(pkcs11-tool --module $MODULE --slot $SLOT_ID \
  --login --pin $PIN -O 2>/dev/null | grep "label.*$KEY_LABEL" || true)

if [ -z "$CHAVE" ]; then
  echo "=== Gerando par de chaves RSA 2048 ==="
  pkcs11-tool --module $MODULE --slot $SLOT_ID \
    --login --pin $PIN \
    --keypairgen --key-type rsa:2048 \
    --id $KEY_ID --label $KEY_LABEL
else
  echo "Chave '$KEY_LABEL' já existe, pulando geração."
fi

# VERIFICA SE CERTIFICADO JÁ EXISTE
CERT=$(pkcs11-tool --module $MODULE --slot $SLOT_ID \
  --login --pin $PIN -O 2>/dev/null | grep -A1 "Certificate Object" | grep "label.*$KEY_LABEL" || true)

if [ -z "$CERT" ]; then
  echo "=== Gerando e importando certificado ==="
  openssl req -new -x509 \
    -newkey rsa:2048 \
    -keyout /tmp/temp-key.pem \
    -out /tmp/assinador-cert.pem \
    -days 3650 -nodes \
    -subj "/CN=Fulano/O=Empresa/serialNumber=CPF:98765432100"

  openssl x509 -in /tmp/assinador-cert.pem \
    -outform DER -out $CERT_OUT

  pkcs11-tool --module $MODULE --slot $SLOT_ID \
    --login --pin $PIN \
    --write-object $CERT_OUT \
    --type cert --id $KEY_ID --label $KEY_LABEL

  rm -f /tmp/temp-key.pem /tmp/assinador-cert.pem $CERT_OUT
  echo "Certificado importado."
else
  echo "Certificado já existe, pulando importação."
fi

echo "=== Setup concluído ==="
pkcs11-tool --module $MODULE --slot $SLOT_ID --login --pin $PIN -O