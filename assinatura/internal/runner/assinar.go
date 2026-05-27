package runner

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var (
	assinarBundle     string
	assinarProvenance string
	assinarPin        string
)

var assinarCmd = &cobra.Command{
	Use:   "assinar",
	Short: "Cria uma assinatura digital no padrão FHIR",
	Long: `Cria uma assinatura digital no padrão FHIR da SES-GO.

Invoca o assinador.jar passando o Bundle, o Provenance e o PIN
do dispositivo criptográfico. O resultado é exibido em stdout
como um recurso FHIR Signature em JSON.

Exemplo:
  assinatura assinar --bundle bundle.json --provenance provenance.json --pin 1234`,
	RunE: func(cmd *cobra.Command, args []string) error {
		// Validação: verificar se os arquivos existem
		if err := validarArquivoExiste(assinarBundle, "--bundle"); err != nil {
			return err
		}
		if err := validarArquivoExiste(assinarProvenance, "--provenance"); err != nil {
			return err
		}
		// Validação: PIN deve conter apenas dígitos e ter entre 4 e 8 caracteres
		if err := validarPin(assinarPin); err != nil {
			return err
		}

		// TODO (Sprint 2): invocar assinador.jar via exec.Command e capturar stdout
		fmt.Fprintln(os.Stderr, "Invocação do assinador.jar será implementada na Sprint 2.")
		return nil
	},
}

func init() {
	assinarCmd.Flags().StringVarP(&assinarBundle, "bundle", "b", "", "Caminho do arquivo JSON Bundle (obrigatório)")
	assinarCmd.Flags().StringVarP(&assinarProvenance, "provenance", "v", "", "Caminho do arquivo JSON Provenance (obrigatório)")
	assinarCmd.Flags().StringVarP(&assinarPin, "pin", "p", "", "PIN numérico do dispositivo PKCS#11, entre 4 e 8 dígitos (obrigatório)")

	_ = assinarCmd.MarkFlagRequired("bundle")
	_ = assinarCmd.MarkFlagRequired("provenance")
	_ = assinarCmd.MarkFlagRequired("pin")
}
