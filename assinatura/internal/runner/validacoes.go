package runner

import (
	"fmt"
	"os"
	"unicode"
)

// validarArquivoExiste verifica se o arquivo indicado pelo caminho existe.
// Retorna erro com mensagem orientativa se não existir.
func validarArquivoExiste(caminho string, flag string) error {
	if _, err := os.Stat(caminho); os.IsNotExist(err) {
		return fmt.Errorf("arquivo não encontrado para %s: %q\nVerifique se o caminho está correto", flag, caminho)
	}
	return nil
}

// validarPin verifica se o PIN contém apenas dígitos e tem entre 4 e 8 caracteres.
// Retorna erro com mensagem orientativa se inválido.
func validarPin(pin string) error {
	if len(pin) < 4 || len(pin) > 8 {
		return fmt.Errorf("PIN inválido para --pin: deve ter entre 4 e 8 dígitos numéricos (recebido: %q com %d caractere(s))", pin, len(pin))
	}
	for _, c := range pin {
		if !unicode.IsDigit(c) {
			return fmt.Errorf("PIN inválido para --pin: deve conter apenas dígitos numéricos (caractere inválido encontrado: %q)", c)
		}
	}
	return nil
}
