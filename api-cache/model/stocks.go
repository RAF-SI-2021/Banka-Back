package model

type IntradayStocks struct {
	Ticker string  `csv:"-"`
	Time   string  `csv:"time"`
	Open   float64 `csv:"open"`
	High   float64 `csv:"high"`
	Low    float64 `csv:"low"`
	Close  float64 `csv:"close"`
	Volume int64   `csv:"volume"`
}

type PeriodicStocks struct {
	Ticker    string  `csv:"-"`
	Timestamp string  `csv:"timestamp"`
	Open      float64 `csv:"open"`
	High      float64 `csv:"high"`
	Low       float64 `csv:"low"`
	Close     float64 `csv:"close"`
	Volume    int64   `csv:"volume"`
}
