package runner

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "assinatura",
	Short: "CLI do Sistema Runner para assinatura digital FHIR",
	Long: `assinatura é o CLI do Sistema Runner.

Permite criar e validar assinaturas digitais no padrão FHIR
da SES-GO, invocando o assinador.jar como processo externo.`,
}

// Execute é chamada pelo main.go e executa o comando raiz.
func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}

func init() {
	rootCmd.AddCommand(assinarCmd)
	rootCmd.AddCommand(validarCmd)
	rootCmd.AddCommand(versionCmd)
}
