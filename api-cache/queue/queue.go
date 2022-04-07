package queue

import (
	"sync"

	"xmudrii.com/api-cache/model"
)

type Queue struct {
	Array []model.AlphaVantageRequest
	lock  *sync.Mutex
}

var QueueImpl *Queue

func NewQueue() {
	QueueImpl = &Queue{
		Array: make([]model.AlphaVantageRequest, 0),
		lock:  &sync.Mutex{},
	}
}

func (q *Queue) IsEmpty() bool {
	q.lock.Lock()
	val := len(q.Array) == 0
	q.lock.Unlock()
	return val
}

func (q *Queue) Add(request model.AlphaVantageRequest) {
	q.lock.Lock()
	q.Array = append(q.Array, request)
	q.lock.Unlock()
}

func (q *Queue) Get() model.AlphaVantageRequest {
	q.lock.Lock()
	req := q.Array[0]
	q.Array = q.Array[1:]
	q.lock.Unlock()
	return req
}
