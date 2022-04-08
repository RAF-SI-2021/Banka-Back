package tsdb

import "xmudrii.com/api-cache/model"

type TSDBStockClient interface {
	PushIntradayStockData(string, []model.IntradayStocks) error
	PushPeriodicStockData(model.AlphaVantageStockRequestType, string, []model.PeriodicStocks) error
}

type TSDBForexClient interface {
	PushIntradayForexData(string, string, []model.IntradayForex) error
	PushPeriodicForexData(model.AlphaVantageForexRequestType, string, string, []model.PeriodicForex) error
}
