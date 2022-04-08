package model

type AlphaVantageStockRequest struct {
	Type     AlphaVantageStockRequestType `json:"type"`
	Symbol   string                       `json:"symbol"`
	Interval string                       `json:"interval"`
	Months   int                          `json:"months"`
	Slices   []string                     `json:"-"`
}

type AlphaVantageStockRequestType string

const (
	IntradayAlphaVantageRequestType AlphaVantageStockRequestType = "intraday"
	DailyAlphaVantageRequestType    AlphaVantageStockRequestType = "daily"
	WeeklyAlphaVantageRequestType   AlphaVantageStockRequestType = "weekly"
	MonthlyAlphaVantageRequestType  AlphaVantageStockRequestType = "monthly"
)

type AlphaVantageForexRequestType string

const (
	IntradayAlphaVantageForexRequestType AlphaVantageForexRequestType = "intraday"
	ExchangeAlphaVantageForexRequestType AlphaVantageForexRequestType = "exhange"
)

type AlphaVantageForexRequest struct {
	Type       AlphaVantageForexRequestType `json:"type"`
	FromSymbol string                       `json:"from"`
	ToSymbol   string                       `json:"to"`
	Interval   string                       `json:"interval"`
	Months     int                          `json:"months"`
	Slices     []string                     `json:"-"`
}
