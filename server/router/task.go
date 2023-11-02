package router

import (
	"encoding/json"
	"net/http"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/task"
	"github.com/whoisnian/noti-bridge/server/transporter"
)

func messageHandler(store *httpd.Store) {
	params := struct {
		GID  string
		Task task.Task
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}

	results := []string{}
	devices := storage.List(params.GID)
	for i := range devices {
		if err := transporter.Notify(&params.Task, &devices[i]); err != nil {
			results = append(results, err.Error())
		} else {
			results = append(results, "ok")
		}
	}

	store.RespondJson(jsonMap{"results": results})
}
