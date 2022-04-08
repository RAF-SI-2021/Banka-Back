package repository

type StocksRepository interface {
	HandleStockRequests()
}

type ForexRepository interface {
	HandleForexRequests()
}
