package model

type PeriodicForex struct {
	FromCode        string  `json:"1. From_Currency Code"`
	FromName        string  `json:"2. From_Currency Name"`
	ToCode          string  `json:"3. To_Currency Code"`
	ToName          string  `json:"4. To_Currency Name"`
	LatestRefreshed string  `json:"6. Last Refreshed"`
	TimeZone        string  `json:"7. Time Zone"`
	ExhangeRate     float64 `json:"5. Exchange Rate"`
	BidPrice        float64 `json:"8. Bid Price"`
	AskPrice        float64 `json:"9. Ask Price"`
}

type IntradayForex struct {
	FromCode string  `csv:"-"`
	ToCode   string  `csv:"-"`
	Time     string  `csv:"time"`
	Open     float64 `csv:"open"`
	High     float64 `csv:"high"`
	Low      float64 `csv:"low"`
	Close    float64 `csv:"close"`
}
