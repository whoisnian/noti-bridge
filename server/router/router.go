package router

import (
	"net/http"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/global"
)

type jsonMap map[string]any

var (
	msgOK = jsonMap{"msg": "ok"}
)

func Setup() *httpd.Mux {
	mux := httpd.NewMux()
	mux.HandleRelay(global.LOG.Relay)

	mux.Handle("/api/device", http.MethodPut, bindHandler)
	mux.Handle("/api/device", http.MethodDelete, unbindHandler)
	mux.Handle("/api/device", http.MethodGet, listHandler)

	mux.Handle("/api/task", http.MethodPost, messageHandler)

	mux.Handle("/status", http.MethodGet, statusHandler)
	return mux
}
