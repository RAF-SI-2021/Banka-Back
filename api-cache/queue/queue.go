package queue

import (
	"sync"

	"xmudrii.com/api-cache/model"
)

type Queue struct {
	StockArray []model.AlphaVantageStockRequest
	ForexArray []model.AlphaVantageForexRequest
	lock       *sync.Mutex
}

var QueueImpl *Queue

func NewQueue() {
	QueueImpl = &Queue{
		StockArray: make([]model.AlphaVantageStockRequest, 0),
		ForexArray: make([]model.AlphaVantageForexRequest, 0),
		lock:       &sync.Mutex{},
	}
}

func (q *Queue) IsEmptyStock() bool {
	q.lock.Lock()
	val := len(q.StockArray) == 0
	q.lock.Unlock()
	return val
}

func (q *Queue) AddStock(request model.AlphaVantageStockRequest) {
	q.lock.Lock()
	q.StockArray = append(q.StockArray, request)
	q.lock.Unlock()
}

func (q *Queue) GetStock() model.AlphaVantageStockRequest {
	q.lock.Lock()
	req := q.StockArray[0]
	q.StockArray = q.StockArray[1:]
	q.lock.Unlock()
	return req
}

func (q *Queue) IsEmptyForex() bool {
	q.lock.Lock()
	val := len(q.ForexArray) == 0
	q.lock.Unlock()
	return val
}

func (q *Queue) AddForex(request model.AlphaVantageForexRequest) {
	q.lock.Lock()
	q.ForexArray = append(q.ForexArray, request)
	q.lock.Unlock()
}

func (q *Queue) GetForex() model.AlphaVantageForexRequest {
	q.lock.Lock()
	req := q.ForexArray[0]
	q.ForexArray = q.ForexArray[1:]
	q.lock.Unlock()
	return req
}
