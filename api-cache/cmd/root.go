package cmd

import (
	"fmt"

	"github.com/spf13/cobra"

	"xmudrii.com/api-cache/queue"
	"xmudrii.com/api-cache/repository"
	"xmudrii.com/api-cache/server"
	"xmudrii.com/api-cache/tsdb"
)

func Execute() error {
	return root().Execute()
}

func root() *cobra.Command {
	opts := &globalOptions{}

	rootCmd := &cobra.Command{
		Use:   "api-cache",
		Short: "Cache Alpha Vantage API calls",
		Long:  "Store data from the Alpha Vantage API to the InfluxDB database for caching purposes",
		RunE: func(cmd *cobra.Command, _ []string) error {
			fmt.Println(opts)
			if opts.alphavantageToken == "" {
				return fmt.Errorf("alpha vantage api token is required")
			}
			if opts.influxUrl == "" {
				return fmt.Errorf("influxdb url is required")
			}
			if opts.influxToken == "" {
				return fmt.Errorf("influxdb token is required")
			}
			if opts.influxOrg == "" {
				return fmt.Errorf("influxdb org is required")
			}
			if opts.influxBucket == "" {
				return fmt.Errorf("influxdb bucket is required")
			}

			// Created a shared queue
			queue.NewQueue()

			// Start request handlers
			tsdbStockClient := tsdb.NewStockInfluxDBClient(opts.influxUrl, opts.influxToken, opts.influxOrg, opts.influxBucket)
			stockRepo := repository.NewAlphaVantageStockRepository(opts.alphavantageToken, tsdbStockClient)

			// tsdbForexClient := tsdb.NewForexInfluxDBClient(opts.influxUrl, opts.influxToken, opts.influxOrg, opts.influxBucket)
			// forexRepo := repository.NewAlphaVantageForexRepository(opts.alphavantageToken, tsdbStockClient)

			go stockRepo.HandleStockRequests()

			// Start server
			server.StartServer(opts.listenAddress)

			return nil
		},
	}

	rootCmd.Flags().StringVarP(&opts.listenAddress,
		"address",
		"a",
		":8000",
		"Address to listen on")

	rootCmd.Flags().StringVarP(&opts.alphavantageToken,
		"av-token",
		"t",
		"",
		"Alpha Vantage API token")

	rootCmd.Flags().StringVarP(&opts.influxUrl,
		"influx-url",
		"",
		"http://localhost:8086",
		"InfluxDB 2 URL")

	rootCmd.Flags().StringVarP(&opts.influxOrg,
		"influx-org",
		"",
		"",
		"InfluxDB Organization")

	rootCmd.Flags().StringVarP(&opts.influxBucket,
		"influx-bucket",
		"",
		"",
		"InfluxDB Bucket")

	rootCmd.Flags().StringVarP(&opts.influxToken,
		"influx-token",
		"",
		"",
		"InfluxDB Token")

	return rootCmd
}
