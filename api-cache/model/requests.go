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
	IntradayAlphaVantageStockRequestType AlphaVantageStockRequestType = "intraday"
	DailyAlphaVantageStockRequestType    AlphaVantageStockRequestType = "daily"
	WeeklyAlphaVantageStockRequestType   AlphaVantageStockRequestType = "weekly"
	MonthlyAlphaVantageStockRequestType  AlphaVantageStockRequestType = "monthly"
)

type AlphaVantageForexRequestType string

const (
	IntradayAlphaVantageForexRequestType AlphaVantageForexRequestType = "intraday"
	DailyAlphaVantageForexRequestType    AlphaVantageForexRequestType = "daily"
	WeeklyAlphaVantageForexRequestType   AlphaVantageForexRequestType = "weekly"
	MonthlyAlphaVantageForexRequestType  AlphaVantageForexRequestType = "monthly"
)

type AlphaVantageForexRequest struct {
	Type       AlphaVantageForexRequestType `json:"type"`
	FromSymbol string                       `json:"from"`
	ToSymbol   string                       `json:"to"`
	Interval   string                       `json:"interval"`
}
