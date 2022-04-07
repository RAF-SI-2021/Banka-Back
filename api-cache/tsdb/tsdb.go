package tsdb

import "xmudrii.com/api-cache/model"

type TSDBClient interface {
	PushIntradayData(string, []model.IntradayStocks) error
	PushPeriodicData(model.AlphaVantageRequestType, string, []model.PeriodicStocks) error
}
