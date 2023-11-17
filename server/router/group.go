package router

import (
	"encoding/json"
	"net/http"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/storage"
)

func bindGroupsHandler(store *httpd.Store) {
	params := struct {
		GIDs  []string
		Type  int64
		Token string
		Name  string
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	if len(params.GIDs) == 0 || params.Token == "" || params.Name == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params GIDs/Token/Name"})
		return
	}

	if err := storage.Bind(params.GIDs, params.Type, params.Token, params.Name); err != nil {
		store.W.WriteHeader(http.StatusUnprocessableEntity)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	store.RespondJson(msgOK)
}

func unbindGroupsHandler(store *httpd.Store) {
	params := struct {
		GIDs  []string
		Type  int64
		Token string
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	if len(params.GIDs) == 0 || params.Token == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params GIDs/Token"})
		return
	}

	if err := storage.UnBind(params.GIDs, params.Type, params.Token); err != nil {
		store.W.WriteHeader(http.StatusUnprocessableEntity)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	store.RespondJson(msgOK)
}