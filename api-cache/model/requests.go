package model

type AlphaVantageRequest struct {
	Type     AlphaVantageRequestType `json:"type"`
	Symbol   string                  `json:"symbol"`
	Interval string                  `json:"interval"`
	Months   int                     `json:"months"`
	Slices   []string                `json:"-"`
}

type AlphaVantageRequestType string

const (
	IntradayAlphaVantageRequestType AlphaVantageRequestType = "intraday"
	DailyAlphaVantageRequestType    AlphaVantageRequestType = "daily"
	WeeklyAlphaVantageRequestType   AlphaVantageRequestType = "weekly"
	MonthlyAlphaVantageRequestType  AlphaVantageRequestType = "monthly"
)
