package router

import "github.com/whoisnian/glb/httpd"

func statusHandler(store *httpd.Store) {
	store.Respond200(nil)
}
