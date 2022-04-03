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

type AlphaVantageRepository struct {
	apiKey     string
	tsdbClient tsdb.TSDBClient
}

func NewAlphaVantageRepository(apiKey string, tsdbClient tsdb.TSDBClient) StocksRepository {
	return &AlphaVantageRepository{
		apiKey:     apiKey,
		tsdbClient: tsdbClient,
	}
}

func (r *AlphaVantageRepository) HandleRequests() {
	for {
		if queue.QueueImpl.IsEmpty() {
			time.Sleep(3 * time.Second)
			continue
		}

		req := queue.QueueImpl.Get()

		if req.Type == model.IntradayAlphaVantageRequestType {
			if err := r.handleIntradayRequest(req); err != nil {
				log.Println(err)
			}
		} else {
			if err := r.handlePeriodicRequest(req); err != nil {
				log.Println(err)
			}
		}

	}
}

func (r *AlphaVantageRepository) handleIntradayRequest(req model.AlphaVantageRequest) error {
	completeData := []model.IntradayStocks{}

	for _, slice := range req.Slices {
		log.Printf("Collecting intraday data for %q, interval of %q and slice %q", req.Symbol, req.Interval, slice)

		data, timeout, err := internalIntraday(r.apiKey, req.Symbol, req.Interval, slice)
		if err != nil {
			return err
		}
		if timeout {
			log.Println("Timeout, waiting 1 minute...")
			time.Sleep(1 * time.Minute)
			data, timeout, err = internalIntraday(r.apiKey, req.Symbol, req.Interval, slice)
			if err != nil {
				return err
			}
			if timeout {
				return fmt.Errorf("timeout")
			}
		}

		completeData = append(completeData, data...)
	}

	if err := r.tsdbClient.PushIntradayData(req.Symbol, completeData); err != nil {
		return err
	}

	return nil
}

func internalIntraday(apiKey, ticker, interval, slice string) ([]model.IntradayStocks, bool, error) {
	url := fmt.Sprintf("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY_EXTENDED&symbol=%s&interval=%s&slice=%s&adjusted=false&apikey=%s", ticker, interval, slice, apiKey)
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

	data := []model.IntradayStocks{}
	if err := gocsv.UnmarshalBytes(raw, &data); err != nil {
		return nil, false, errors.Wrap(err, "failed to parse csv file")
	}

	return data, false, nil
}

func (r *AlphaVantageRepository) handlePeriodicRequest(req model.AlphaVantageRequest) error {
	completeData := []model.PeriodicStocks{}

	log.Printf("Collecting %s data for %q", req.Type, req.Symbol)

	for _, slice := range req.Slices {
		log.Printf("Collecting intraday data for %q, interval of %q and slice %q", req.Symbol, req.Interval, slice)

		data, timeout, err := internalPeriodic(req.Type, r.apiKey, req.Symbol)
		if err != nil {
			return err
		}
		if timeout {
			log.Println("Timeout, waiting 1 minute...")
			time.Sleep(1 * time.Minute)
			data, timeout, err = internalPeriodic(req.Type, r.apiKey, req.Symbol)
			if err != nil {
				return err
			}
			if timeout {
				return fmt.Errorf("timeout")
			}
		}

		completeData = append(completeData, data...)
	}

	if err := r.tsdbClient.PushPeriodicData(req.Type, req.Symbol, completeData); err != nil {
		return err
	}

	return nil
}

func internalPeriodic(reqType model.AlphaVantageRequestType, apiKey, ticker string) ([]model.PeriodicStocks, bool, error) {
	url := ""
	if reqType == model.DailyAlphaVantageRequestType {
		url = fmt.Sprintf("https://www.alphavantage.co/query?function=%s&symbol=%s&outputsize=full&datatype=csv&apikey=%s", alphaVantageFunction(reqType), ticker, apiKey)
	} else {
		url = fmt.Sprintf("https://www.alphavantage.co/query?function=%s&symbol=%s&datatype=csv&apikey=%s", alphaVantageFunction(reqType), ticker, apiKey)
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

	data := []model.PeriodicStocks{}
	if err := gocsv.UnmarshalBytes(raw, &data); err != nil {
		return nil, false, errors.Wrap(err, "failed to parse csv file")
	}

	return data, false, nil
}

func alphaVantageFunction(t model.AlphaVantageRequestType) string {
	switch t {
	case model.IntradayAlphaVantageRequestType:
		return "TIME_SERIES_INTRADAY_EXTENDED"
	case model.DailyAlphaVantageRequestType:
		return "TIME_SERIES_DAILY"
	case model.WeeklyAlphaVantageRequestType:
		return "TIME_SERIES_WEEKLY"
	case model.MonthlyAlphaVantageRequestType:
		return "TIME_SERIES_MONTHLY"
	default:
		return ""
	}
}
