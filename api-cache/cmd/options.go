package cmd

type globalOptions struct {
	listenAddress string

	alphavantageToken string

	influxUrl    string
	influxToken  string
	influxOrg    string
	influxBucket string
}
