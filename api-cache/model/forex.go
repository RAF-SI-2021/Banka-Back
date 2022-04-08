package model

type PeriodicForex struct {
	FromCode  string  `csv:"-"`
	ToCode    string  `csv:"-"`
	Timestamp string  `csv:"timestamp"`
	Open      float64 `csv:"open"`
	High      float64 `csv:"high"`
	Low       float64 `csv:"low"`
	Close     float64 `csv:"close"`
}

type IntradayForex struct {
	FromCode  string  `csv:"-"`
	ToCode    string  `csv:"-"`
	Timestamp string  `csv:"timestamp"`
	Open      float64 `csv:"open"`
	High      float64 `csv:"high"`
	Low       float64 `csv:"low"`
	Close     float64 `csv:"close"`
}
