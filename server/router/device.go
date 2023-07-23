package router

import (
	"encoding/json"
	"net/http"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/storage"
)

func bindHandler(store *httpd.Store) {
	params := struct {
		GID   string
		Type  int64
		Token string
		Name  string
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	if params.GID == "" || params.Token == "" || params.Name == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params GID/Token/Name"})
		return
	}

	if err := storage.Bind(params.GID, params.Type, params.Token, params.Name); err != nil {
		store.W.WriteHeader(http.StatusUnprocessableEntity)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	store.RespondJson(nil)
}

func unbindHandler(store *httpd.Store) {
	params := struct {
		GID   string
		Type  int64
		Token string
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	if params.GID == "" || params.Token == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params GID/Token"})
		return
	}

	if err := storage.UnBind(params.GID, params.Type, params.Token); err != nil {
		store.W.WriteHeader(http.StatusUnprocessableEntity)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	store.RespondJson(nil)
}

func listHandler(store *httpd.Store) {
	gid := store.R.FormValue("GID")
	if gid == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "require gid param"})
		return
	}

	store.RespondJson(storage.List(gid))
}
