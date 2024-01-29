package router

import (
	"io"
	"log/slog"
	"mime"
	"net/http"
	"os"
	"path/filepath"
	"strconv"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/transporter"
	"github.com/whoisnian/noti-bridge/server/web"
)

func serveWebFile(store *httpd.Store, path string) {
	file, err := web.FS.Open(path)
	if err != nil {
		if os.IsNotExist(err) {
			store.W.WriteHeader(http.StatusNotFound)
			return
		}
		global.LOG.Error("web.FS.Open error", slog.Any("error", err), slog.String("tid", store.GetID()))
		store.W.WriteHeader(http.StatusInternalServerError)
		return
	}
	defer file.Close()

	info, err := file.Stat()
	if err != nil {
		global.LOG.Error("file.Stat error", slog.Any("error", err), slog.String("tid", store.GetID()))
		store.W.WriteHeader(http.StatusInternalServerError)
		return
	} else if info.IsDir() {
		store.W.WriteHeader(http.StatusForbidden)
		return
	}

	ctype := mime.TypeByExtension(filepath.Ext(path))
	if ctype == "" {
		ctype = "application/octet-stream"
	}
	store.W.Header().Set("Content-Type", ctype)

	if store.W.Header().Get("Content-Encoding") == "" {
		store.W.Header().Set("Content-Length", strconv.FormatInt(info.Size(), 10))
	}
	if _, err := io.CopyN(store.W, file, info.Size()); err != nil {
		global.LOG.Error("io.CopyN failed", slog.Any("error", err), slog.String("tid", store.GetID()))
		store.W.WriteHeader(http.StatusInternalServerError)
		return
	}
}

func webHandler(store *httpd.Store) {
	serveWebFile(store, "index.html")
}

func staticHandler(store *httpd.Store) {
	serveWebFile(store, filepath.Join("static", store.RouteParamAny()))
}

func serverKeyHandler(store *httpd.Store) {
	store.Respond200([]byte(transporter.BrowserServerKey()))
}
