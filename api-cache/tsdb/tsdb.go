package tsdb

import "xmudrii.com/api-cache/model"

type TSDBStockClient interface {
	PushIntradayStockData(string, []model.IntradayStocks) error
	PushPeriodicStockData(model.AlphaVantageStockRequestType, string, []model.PeriodicStocks) error
}

type TSDBForexClient interface {
	PushIntradayForexkData(string, []model.IntradayForex) error
	PushPeriodicForexkData(model.AlphaVantageForexRequestType, string, []model.PeriodicForex) error
}
