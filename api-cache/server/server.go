package server

import (
	"log"
	"net/http"

	"github.com/gorilla/mux"

	"xmudrii.com/api-cache/server/handler"
)

func StartServer(listenAddress string) {
	router := mux.NewRouter()

	router.HandleFunc("/alphavantage", handler.AlphaVantageData).Methods(http.MethodPost)

	log.Println("API is running!")
	http.ListenAndServe(listenAddress, router)
}
