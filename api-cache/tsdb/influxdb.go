package tsdb

import (
	"context"
	"log"
	"time"

	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/influxdata/influxdb-client-go/v2/api/write"
	"github.com/pkg/errors"

	"xmudrii.com/api-cache/model"
)

type InfluxDBClient struct {
	client influxdb2.Client

	org    string
	bucket string
}

func NewInfluxDBClient(url, token, org, bucket string) TSDBClient {
	log.Println(url)
	client := influxdb2.NewClient(url, token)
	client.Options().WriteOptions().SetUseGZip(true)

	return &InfluxDBClient{
		client: client,
		org:    org,
		bucket: bucket,
	}
}

func (c InfluxDBClient) PushIntradayData(ticker string, data []model.IntradayStocks) error {
	writeAPI := c.client.WriteAPIBlocking(c.org, c.bucket)

	points := []*write.Point{}
	for _, d := range data {
		t, err := time.Parse("2006-01-02 15:04:05", d.Time)
		if err != nil {
			return errors.Wrap(err, "converting time")
		}

		p := influxdb2.NewPoint("stock_price_intraday",
			map[string]string{"ticker": ticker},
			map[string]interface{}{
				"open":   d.Open,
				"high":   d.High,
				"low":    d.Low,
				"close":  d.Close,
				"volume": d.Volume,
			},
			t)

		points = append(points, p)
	}

	if err := writeAPI.WritePoint(context.Background(), points...); err != nil {
		return errors.Wrap(err, "failed to push to influxdb")
	}

	log.Println("Push successful...")

	return nil
}

func (c InfluxDBClient) PushPeriodicData(reqType model.AlphaVantageRequestType, ticker string, data []model.PeriodicStocks) error {
	writeAPI := c.client.WriteAPIBlocking(c.org, c.bucket)

	points := []*write.Point{}
	for _, d := range data {
		t, err := time.Parse("2006-01-02", d.Timestamp)
		if err != nil {
			return errors.Wrap(err, "converting time")
		}

		p := influxdb2.NewPoint("stock_price_"+string(reqType),
			map[string]string{"ticker": ticker},
			map[string]interface{}{
				"open":   d.Open,
				"high":   d.High,
				"low":    d.Low,
				"close":  d.Close,
				"volume": d.Volume,
			},
			t)

		points = append(points, p)
	}

	if err := writeAPI.WritePoint(context.Background(), points...); err != nil {
		return errors.Wrap(err, "failed to push to influxdb")
	}

	log.Println("Push successful...")

	return nil
}
