package runner

import (
	"os"
	"path/filepath"
	"testing"
)

// ---------------------------------------------------------------------------
// Testes de validarPin
// ---------------------------------------------------------------------------

func TestValidarPin_Valido(t *testing.T) {
	casos := []string{"1234", "12345678", "0000", "99999"}
	for _, pin := range casos {
		if err := validarPin(pin); err != nil {
			t.Errorf("esperava PIN válido para %q, mas obteve erro: %v", pin, err)
		}
	}
}

func TestValidarPin_Curtodemais(t *testing.T) {
	if err := validarPin("123"); err == nil {
		t.Error("esperava erro para PIN com 3 dígitos, mas não obteve")
	}
}

func TestValidarPin_Longodemais(t *testing.T) {
	if err := validarPin("123456789"); err == nil {
		t.Error("esperava erro para PIN com 9 dígitos, mas não obteve")
	}
}

func TestValidarPin_ComLetras(t *testing.T) {
	if err := validarPin("12ab"); err == nil {
		t.Error("esperava erro para PIN com letras, mas não obteve")
	}
}

func TestValidarPin_Vazio(t *testing.T) {
	if err := validarPin(""); err == nil {
		t.Error("esperava erro para PIN vazio, mas não obteve")
	}
}

// ---------------------------------------------------------------------------
// Testes de validarArquivoExiste
// ---------------------------------------------------------------------------

func TestValidarArquivoExiste_Existe(t *testing.T) {
	// Cria um arquivo temporário real para o teste
	tmp, err := os.CreateTemp(t.TempDir(), "bundle-*.json")
	if err != nil {
		t.Fatalf("erro ao criar arquivo temporário: %v", err)
	}
	tmp.Close()

	if err := validarArquivoExiste(tmp.Name(), "--bundle"); err != nil {
		t.Errorf("esperava sucesso para arquivo existente, mas obteve erro: %v", err)
	}
}

func TestValidarArquivoExiste_NaoExiste(t *testing.T) {
	caminho := filepath.Join(t.TempDir(), "nao-existe.json")
	if err := validarArquivoExiste(caminho, "--bundle"); err == nil {
		t.Error("esperava erro para arquivo inexistente, mas não obteve")
	}
}

// ---------------------------------------------------------------------------
// Testes de parsing dos comandos (flags obrigatórias)
// ---------------------------------------------------------------------------

func TestAssinarCmd_FlagsObrigatorias(t *testing.T) {
	// Sem nenhuma flag: deve falhar
	assinarCmd.ResetFlags()
	init_assinar()

	err := assinarCmd.Execute()
	if err == nil {
		t.Error("esperava erro ao executar assinar sem flags, mas não obteve")
	}
}

func TestValidarCmd_FlagsObrigatorias(t *testing.T) {
	// Sem nenhuma flag: deve falhar
	validarCmd.ResetFlags()
	init_validar()

	err := validarCmd.Execute()
	if err == nil {
		t.Error("esperava erro ao executar validar sem flags, mas não obteve")
	}
}

// init_assinar e init_validar reregistram as flags após ResetFlags nos testes.
func init_assinar() {
	assinarCmd.Flags().StringVarP(&assinarBundle, "bundle", "b", "", "Caminho do arquivo JSON Bundle (obrigatório)")
	assinarCmd.Flags().StringVarP(&assinarProvenance, "provenance", "v", "", "Caminho do arquivo JSON Provenance (obrigatório)")
	assinarCmd.Flags().StringVarP(&assinarPin, "pin", "p", "", "PIN numérico do dispositivo PKCS#11, entre 4 e 8 dígitos (obrigatório)")
	_ = assinarCmd.MarkFlagRequired("bundle")
	_ = assinarCmd.MarkFlagRequired("provenance")
	_ = assinarCmd.MarkFlagRequired("pin")
}

func init_validar() {
	validarCmd.Flags().StringVarP(&validarSignature, "signature", "s", "", "Caminho do arquivo JSON Signature (obrigatório)")
	validarCmd.Flags().StringVarP(&validarBundle, "bundle", "b", "", "Caminho do arquivo JSON Bundle original (obrigatório)")
	_ = validarCmd.MarkFlagRequired("signature")
	_ = validarCmd.MarkFlagRequired("bundle")
}
