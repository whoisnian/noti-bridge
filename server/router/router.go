package router

import (
	"net/http"

	"github.com/whoisnian/glb/httpd"
)

func Setup() *httpd.Mux {
	mux := httpd.NewMux()
	mux.Handle("/status", http.MethodGet, statusHandler)
	return mux
}
