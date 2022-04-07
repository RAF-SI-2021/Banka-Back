package main

import (
	"fmt"
	"os"

	"xmudrii.com/api-cache/cmd"
)

func main() {
	if err := cmd.Execute(); err != nil {
		fmt.Fprintf(os.Stderr, "error: %s\n", err)
	}
}
