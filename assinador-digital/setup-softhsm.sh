#!/bin/bash
set -euo pipefail

MODULE=/usr/lib/softhsm/libsofthsm2.so
SLOT_LABEL="MeuToken"
PIN="1234"
SO_PIN="123456"
KEY_LABEL="minha-chave"
KEY_ID="01"
CERT_DER=/tmp/assinador-cert.der

# --- Verifica dependências ---
falha_dep() {
    echo "ERRO: '$1' não encontrado."
    echo "       Instale com: $2"
    exit 1
}
command -v softhsm2-util &>/dev/null || falha_dep "softhsm2-util" "sudo apt install softhsm2   # Debian/Ubuntu"
command -v pkcs11-tool   &>/dev/null || falha_dep "pkcs11-tool"   "sudo apt install opensc       # Debian/Ubuntu"
command -v openssl       &>/dev/null || falha_dep "openssl"       "sudo apt install openssl      # Debian/Ubuntu"
[ -f "$MODULE" ] || { echo "ERRO: $MODULE não encontrado."; echo "       Instale com: sudo apt install softhsm2"; exit 1; }

# --- Configura diretório do usuário ---
SOFTHSM2_CONF_DIR="$HOME/.config/softhsm2"
SOFTHSM2_TOKEN_DIR="$HOME/.local/share/softhsm2/tokens"
export SOFTHSM2_CONF="$SOFTHSM2_CONF_DIR/softhsm2.conf"

if [ ! -f "$SOFTHSM2_CONF" ]; then
    echo "=== Criando configuração do SoftHSM2 para $USER ==="
    mkdir -p "$SOFTHSM2_CONF_DIR" "$SOFTHSM2_TOKEN_DIR"
    cat > "$SOFTHSM2_CONF" <<EOF
directories.tokendir = $SOFTHSM2_TOKEN_DIR/
objectstore.backend = file
log.level = ERROR
slots.removable = false
EOF
    echo "Configuração criada em $SOFTHSM2_CONF"
fi

# --- Inicializa o token se não existir ---
if ! softhsm2-util --show-slots 2>/dev/null | grep -q "Label:.*${SLOT_LABEL}"; then
    echo "=== Inicializando token '$SLOT_LABEL' ==="
    softhsm2-util --init-token --free \
        --label "$SLOT_LABEL" \
        --pin "$PIN" \
        --so-pin "$SO_PIN"
    echo "Token inicializado."
else
    echo "Token '$SLOT_LABEL' já existe."
fi

# --- Encontra o slot ID real via softhsm2-util ---
# pkcs11-tool --slot recebe o CK_SLOT_ID real (ex: 1657689331), não o índice
# exibido por pkcs11-tool -L. softhsm2-util --show-slots mostra o ID correto.
SLOT_ID=$(softhsm2-util --show-slots 2>/dev/null \
    | awk -v lbl="$SLOT_LABEL" \
        '/^Slot [0-9]/{s=$2} /Label:/ && index($0,lbl){print s; exit}')

if [ -z "$SLOT_ID" ]; then
    echo "ERRO: não foi possível encontrar o slot do token '$SLOT_LABEL'."
    exit 1
fi
echo "Usando slot: $SLOT_ID"

# --- Gera par de chaves RSA 2048 se não existir ---
CHAVE=$(pkcs11-tool --module "$MODULE" --slot "$SLOT_ID" \
    --login --pin "$PIN" -O 2>/dev/null \
    | grep "label.*${KEY_LABEL}" || true)

if [ -z "$CHAVE" ]; then
    echo "=== Gerando par de chaves RSA 2048 no token ==="
    pkcs11-tool --module "$MODULE" --slot "$SLOT_ID" \
        --login --pin "$PIN" \
        --keypairgen --key-type rsa:2048 \
        --id "$KEY_ID" --label "$KEY_LABEL"
else
    echo "Chave '$KEY_LABEL' já existe, pulando geração."
fi

# --- Importa certificado com CPF se não existir ---
CERT=$(pkcs11-tool --module "$MODULE" --slot "$SLOT_ID" \
    --login --pin "$PIN" -O 2>/dev/null \
    | grep -A1 "Certificate Object" | grep "label.*${KEY_LABEL}" || true)

if [ -z "$CERT" ]; then
    echo "=== Gerando e importando certificado com CPF ==="
    openssl req -new -x509 \
        -newkey rsa:2048 \
        -keyout /tmp/temp-key.pem \
        -out /tmp/assinador-cert.pem \
        -days 3650 -nodes \
        -subj "/CN=Fulano/O=Empresa/serialNumber=CPF:98765432100"

    openssl x509 -in /tmp/assinador-cert.pem \
        -outform DER -out "$CERT_DER"

    pkcs11-tool --module "$MODULE" --slot "$SLOT_ID" \
        --login --pin "$PIN" \
        --write-object "$CERT_DER" \
        --type cert --id "$KEY_ID" --label "$KEY_LABEL"

    rm -f /tmp/temp-key.pem /tmp/assinador-cert.pem "$CERT_DER"
    echo "Certificado importado."
else
    echo "Certificado '$KEY_LABEL' já existe, pulando importação."
fi

echo ""
echo "=== Setup concluído ==="
pkcs11-tool --module "$MODULE" --slot "$SLOT_ID" --login --pin "$PIN" -O
