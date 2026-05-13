package runner

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var (
	validarSignature string
	validarBundle    string
)

var validarCmd = &cobra.Command{
	Use:   "validar",
	Short: "Valida uma assinatura digital no padrão FHIR",
	Long: `Valida uma assinatura digital no padrão FHIR da SES-GO.

Invoca o assinador.jar passando o Signature e o Bundle original.
O resultado é exibido em stdout como um recurso FHIR OperationOutcome em JSON.

Exemplo:
  assinatura validar --signature signature.json --bundle bundle.json`,
	RunE: func(cmd *cobra.Command, args []string) error {
		// Validação: verificar se os arquivos existem
		if err := validarArquivoExiste(validarSignature, "--signature"); err != nil {
			return err
		}
		if err := validarArquivoExiste(validarBundle, "--bundle"); err != nil {
			return err
		}

		// TODO (Sprint 2): invocar assinador.jar via exec.Command e capturar stdout
		fmt.Fprintln(os.Stderr, "Invocação do assinador.jar será implementada na Sprint 2.")
		return nil
	},
}

func init() {
	validarCmd.Flags().StringVarP(&validarSignature, "signature", "s", "", "Caminho do arquivo JSON Signature (obrigatório)")
	validarCmd.Flags().StringVarP(&validarBundle, "bundle", "b", "", "Caminho do arquivo JSON Bundle original (obrigatório)")

	_ = validarCmd.MarkFlagRequired("signature")
	_ = validarCmd.MarkFlagRequired("bundle")
}
