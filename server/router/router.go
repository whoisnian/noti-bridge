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

	// page
	mux.Handle("/web", http.MethodGet, webHandler)
	mux.Handle("/static/*", http.MethodGet, staticHandler)
	mux.Handle("/server-key", http.MethodGet, serverKeyHandler)

	// api
	mux.Handle("/api/device", http.MethodPost, updateDeviceHandler)
	mux.Handle("/api/device", http.MethodDelete, deleteDeviceHandler)
	mux.Handle("/api/group", http.MethodPut, bindGroupsHandler)
	mux.Handle("/api/group", http.MethodDelete, unbindGroupsHandler)
	mux.Handle("/api/task", http.MethodPost, taskHandler)

	// status
	mux.Handle("/status", http.MethodGet, statusHandler)
	return mux
}
