package router

import (
	"net/http"

	"github.com/whoisnian/glb/httpd"
)

type jsonMap map[string]any

func Setup() *httpd.Mux {
	mux := httpd.NewMux()

	mux.Handle("/api/device", http.MethodPut, bindHandler)
	mux.Handle("/api/device", http.MethodDelete, unbindHandler)
	mux.Handle("/api/device", http.MethodGet, listHandler)

	mux.Handle("/api/task", http.MethodPost, messageHandler)

	mux.Handle("/status", http.MethodGet, statusHandler)
	return mux
}
