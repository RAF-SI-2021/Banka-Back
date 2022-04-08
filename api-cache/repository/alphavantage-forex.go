package repository

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/gocarina/gocsv"
	"github.com/pkg/errors"

	"xmudrii.com/api-cache/model"
	"xmudrii.com/api-cache/queue"
	"xmudrii.com/api-cache/tsdb"
)

type AlphaVantageForexRepository struct {
	apiKey     string
	tsdbClient tsdb.TSDBForexClient
}

func NewAlphaVantageForexRepository(apiKey string, tsdbClient tsdb.TSDBForexClient) ForexRepository {
	return &AlphaVantageForexRepository{
		apiKey:     apiKey,
		tsdbClient: tsdbClient,
	}
}

func (r *AlphaVantageForexRepository) HandleForexRequests() {
	for {
		if queue.QueueImpl.IsEmptyForex() {
			time.Sleep(3 * time.Second)
			continue
		}

		req := queue.QueueImpl.GetForex()

		if req.Type == model.IntradayAlphaVantageForexRequestType {
			if err := r.handleIntradayForexRequest(req); err != nil {
				log.Println(err)
			}
		} else {
			if err := r.handlePeriodicForexRequest(req); err != nil {
				log.Println(err)
			}
		}

	}
}

func (r *AlphaVantageForexRepository) handleIntradayForexRequest(req model.AlphaVantageForexRequest) error {
	completeData := []model.IntradayForex{}

	log.Printf("Collecting intraday data for %q-%q, interval of", req.FromSymbol, req.ToSymbol)

	data, timeout, err := internalForexIntraday(r.apiKey, req.FromSymbol, req.ToSymbol, req.Interval)
	if err != nil {
		return err
	}
	if timeout {
		log.Println("Timeout, waiting 1 minute...")
		time.Sleep(1 * time.Minute)
		data, timeout, err = internalForexIntraday(r.apiKey, req.FromSymbol, req.ToSymbol, req.Interval)
		if err != nil {
			return err
		}
		if timeout {
			return fmt.Errorf("timeout")
		}
	}

	completeData = append(completeData, data...)

	if err := r.tsdbClient.PushIntradayForexData(req.FromSymbol, req.ToSymbol, completeData); err != nil {
		return err
	}

	return nil
}

func internalForexIntraday(apiKey, from, to, interval string) ([]model.IntradayForex, bool, error) {
	url := fmt.Sprintf("https://www.alphavantage.co/query?function=FX_INTRADAY&from_symbol=%s&to_symbol=%s&interval=%s&apikey=%s&outputsize=full&datatype=csv", from, to, interval, apiKey)
	log.Println(url)
	rawResp, err := http.Get(url)
	if err != nil {
		return nil, false, errors.Wrap(err, "failed to get weather data via api")
	}
	defer rawResp.Body.Close()

	if rawResp.StatusCode != 200 {
		return nil, false, errors.Errorf("got api code %q but expected \"200\"", rawResp.StatusCode)
	}

	raw, err := ioutil.ReadAll(rawResp.Body)
	if err != nil {
		return nil, false, errors.Wrap(err, "failed to parse answer")
	}

	if strings.Contains(string(raw), "Thank you") {
		return nil, true, nil
	}

	data := []model.IntradayForex{}
	if err := gocsv.UnmarshalBytes(raw, &data); err != nil {
		return nil, false, errors.Wrap(err, "failed to parse csv file")
	}

	return data, false, nil
}

func (r *AlphaVantageForexRepository) handlePeriodicForexRequest(req model.AlphaVantageForexRequest) error {
	completeData := []model.PeriodicForex{}

	log.Printf("Collecting %s data for %q %q", req.Type, req.FromSymbol, req.ToSymbol)

	data, timeout, err := internalPeriodicForex(req.Type, r.apiKey, req.FromSymbol, req.ToSymbol)
	if err != nil {
		return err
	}
	if timeout {
		log.Println("Timeout, waiting 1 minute...")
		time.Sleep(1 * time.Minute)
		data, timeout, err = internalPeriodicForex(req.Type, r.apiKey, req.FromSymbol, req.ToSymbol)
		if err != nil {
			return err
		}
		if timeout {
			return fmt.Errorf("timeout")
		}
	}

	completeData = append(completeData, data...)

	if err := r.tsdbClient.PushPeriodicForexData(req.Type, req.FromSymbol, req.ToSymbol, completeData); err != nil {
		return err
	}

	return nil
}

func internalPeriodicForex(reqType model.AlphaVantageForexRequestType, apiKey, from string, to string) ([]model.PeriodicForex, bool, error) {
	url := ""
	if reqType == model.DailyAlphaVantageForexRequestType {
		url = fmt.Sprintf("https://www.alphavantage.co/query?function=%s&from_symbol=%s&to_symbol=%s&outputsize=full&datatype=csv&apikey=%s", alphaVantageForexFunction(reqType), from, to, apiKey)
	} else {
		url = fmt.Sprintf("https://www.alphavantage.co/query?function=%s&from_symbol=%s&to_symbol=%s&datatype=csv&apikey=%s", alphaVantageForexFunction(reqType), from, to, apiKey)
	}

	log.Println(url)
	rawResp, err := http.Get(url)
	if err != nil {
		return nil, false, errors.Wrap(err, "failed to get weather data via api")
	}
	defer rawResp.Body.Close()

	if rawResp.StatusCode != 200 {
		return nil, false, errors.Errorf("got api code %q but expected \"200\"", rawResp.StatusCode)
	}

	raw, err := ioutil.ReadAll(rawResp.Body)
	if err != nil {
		return nil, false, errors.Wrap(err, "failed to parse answer")
	}

	if strings.Contains(string(raw), "Thank you") {
		return nil, true, nil
	}

	data := []model.PeriodicForex{}
	if err := gocsv.UnmarshalBytes(raw, &data); err != nil {
		return nil, false, errors.Wrap(err, "failed to parse csv file")
	}

	return data, false, nil
}

func alphaVantageForexFunction(t model.AlphaVantageForexRequestType) string {
	switch t {
	case model.IntradayAlphaVantageForexRequestType:
		return "FX_INTRADAY"
	case model.DailyAlphaVantageForexRequestType:
		return "FX_DAILY"
	case model.WeeklyAlphaVantageForexRequestType:
		return "FX_WEEKLY"
	case model.MonthlyAlphaVantageForexRequestType:
		return "FX_MONTHLY"
	default:
		return ""
	}
}
