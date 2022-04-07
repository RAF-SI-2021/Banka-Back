package handler

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"

	"xmudrii.com/api-cache/model"
	"xmudrii.com/api-cache/queue"
)

func AlphaVantageData(w http.ResponseWriter, r *http.Request) {
	// Read to request body
	defer r.Body.Close()
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Error requesting intraday data"))
	}

	var req model.AlphaVantageRequest
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

	if req.Type == model.IntradayAlphaVantageRequestType {
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

	queue.QueueImpl.Add(req)
	w.WriteHeader(http.StatusAccepted)
}
