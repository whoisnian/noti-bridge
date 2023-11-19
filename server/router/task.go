package router

import (
	"encoding/json"
	"log/slog"
	"net/http"
	"time"

	"github.com/whoisnian/glb/httpd"
	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/task"
	"github.com/whoisnian/noti-bridge/server/transporter"
)

func taskHandler(store *httpd.Store) {
	params := struct {
		GID  string
		Task task.Task
	}{}

	if err := json.NewDecoder(store.R.Body).Decode(&params); err != nil {
		store.W.WriteHeader(http.StatusBadRequest)
		store.RespondJson(jsonMap{"msg": err.Error()})
		return
	}
	global.LOG.Info("start new task", slog.String("gid", params.GID), slog.String("type", params.Task.Type), slog.String("tid", store.GetID()))

	results := []string{}
	devices := storage.List(params.GID)
	for i := range devices {
		start := time.Now()
		if err := transporter.Notify(&params.Task, &devices[i]); err != nil {
			results = append(results, err.Error())
			global.LOG.Warn(err.Error(),
				slog.String("tag", "NOTIFY"),
				slog.Int64("type", devices[i].Type),
				slog.Duration("duration", time.Since(start)),
				slog.String("tid", store.GetID()),
			)
		} else {
			results = append(results, "ok")
			global.LOG.Info("ok",
				slog.String("tag", "NOTIFY"),
				slog.Int64("type", devices[i].Type),
				slog.Duration("duration", time.Since(start)),
				slog.String("tid", store.GetID()),
			)
		}
	}

	store.RespondJson(jsonMap{"results": results})
}
