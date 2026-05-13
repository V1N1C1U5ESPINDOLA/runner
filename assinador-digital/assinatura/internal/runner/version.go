package runner

import (
	"fmt"

	"github.com/spf13/cobra"
)

// Version é a versão atual do CLI.
// Será substituída automaticamente pelo pipeline de build via ldflags.
var Version = "0.1.0"

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Exibe a versão atual do CLI",
	Long:  `Exibe a versão atual do CLI assinatura.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("assinatura versão %s\n", Version)
	},
}
