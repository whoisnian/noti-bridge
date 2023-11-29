package router

import (
	"encoding/json"
	"log/slog"
	"net/http"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/storage"
)

func updateDeviceHandler(store *httpd.Store) {
	params := struct {
		Type  int64
		Token string
		Name  string
		Extra json.RawMessage
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	global.LOG.Info("update device",
		slog.Int64("type", params.Type),
		slog.String("name", params.Name),
		slog.String("token", params.Token),
		slog.String("tid", store.GetID()),
	)

	if params.Token == "" || params.Name == "" {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": "invalid params Token/Name"})
		return
	}

	if err := storage.UpdateDevice(params.Type, params.Token, params.Name, params.Extra); err != nil {
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
	global.LOG.Info("delete device",
		slog.Int64("type", params.Type),
		slog.String("token", params.Token),
		slog.String("tid", store.GetID()),
	)

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
