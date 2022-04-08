package handler

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"

	"xmudrii.com/api-cache/model"
	"xmudrii.com/api-cache/queue"
)

func AlphaVantageStockData(w http.ResponseWriter, r *http.Request) {
	// Read to request body
	defer r.Body.Close()
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Error requesting intraday data"))
	}

	var req model.AlphaVantageStockRequest
	json.Unmarshal(body, &req)

	// Validation
	if string(req.Type) == "" {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Unsupported request type"))
	}
	if req.Symbol == "" {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Symbol is required"))
	}

	if req.Type == model.IntradayAlphaVantageStockRequestType {
		switch req.Interval {
		case "1min":
		case "5min":
		case "15min":
		case "30min":
		case "60min":
		default:
			w.WriteHeader(http.StatusBadRequest)
			w.Write([]byte("Unsupported interval"))
		}

		if req.Months < 1 && req.Months > 24 {
			w.WriteHeader(http.StatusBadRequest)
			w.Write([]byte("Unsupported number of months"))
		}

		yr := 1
		month := 1
		months := req.Months
		slices := []string{}
		for months > 0 {
			slices = append(slices, fmt.Sprintf("year%dmonth%d", yr, month))
			month = month + 1
			if month == 13 {
				month = month - 12
				yr = yr + 1
			}
			months = months - 1
		}
		req.Slices = slices
	}

	queue.QueueImpl.AddStock(req)
	w.WriteHeader(http.StatusAccepted)
}

func AlphaVantageForexData(w http.ResponseWriter, r *http.Request) {
	// Read to request body
	defer r.Body.Close()
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Error requesting intraday data"))
	}

	var req model.AlphaVantageForexRequest
	json.Unmarshal(body, &req)

	// Validation
	if string(req.Type) == "" {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Unsupported request type"))
	}
	if req.FromSymbol == "" {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("From symbol is required"))
	}

	if req.ToSymbol == "" {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("To symbol is required"))
	}

	if req.Type == model.IntradayAlphaVantageForexRequestType {
		switch req.Interval {
		case "1min":
		case "5min":
		case "15min":
		case "30min":
		case "60min":
		default:
			w.WriteHeader(http.StatusBadRequest)
			w.Write([]byte("Unsupported interval"))
		}
	}

	queue.QueueImpl.AddForex(req)
	w.WriteHeader(http.StatusAccepted)
}
