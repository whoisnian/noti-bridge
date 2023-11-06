package router

import (
	"encoding/json"
	"net/http"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/storage"
)

func updateDeviceHandler(store *httpd.Store) {
	params := struct {
		Type  int64
		Token string
		Name  string
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	if params.Token == "" || params.Name == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params Token/Name"})
		return
	}

	if err := storage.UpdateDevice(params.Type, params.Token, params.Name); err != nil {
		store.W.WriteHeader(http.StatusUnprocessableEntity)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	store.RespondJson(msgOK)
}

func deleteDeviceHandler(store *httpd.Store) {
	params := struct {
		Type  int64
		Token string
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	if params.Token == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params Token"})
		return
	}

	if err := storage.DeleteDevice(params.Type, params.Token); err != nil {
		store.W.WriteHeader(http.StatusUnprocessableEntity)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	store.RespondJson(msgOK)
}
