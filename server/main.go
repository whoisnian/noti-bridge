package main

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"time"

	"github.com/whoisnian/glb/util/osutil"
	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/router"
	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/transporter"
)

func main() {
	global.Setup()
	if global.CFG.Version {
		fmt.Printf("%s %s(%s)\n", global.AppName, global.Version, global.BuildTime)
		return
	}

	if err := storage.SetupDataDir(global.CFG.StoragePath); err != nil {
		global.LOG.Fatal(err.Error())
	}
	if err := transporter.SetupBrowser(global.CFG.VAPIDCredFile); err != nil {
		global.LOG.Warn(err.Error())
	}
	if err := transporter.SetupAndroid(global.CFG.FCMCredFile); err != nil {
		global.LOG.Warn(err.Error())
	}

	server := &http.Server{Addr: global.CFG.ListenAddr, Handler: router.Setup()}
	go func() {
		global.LOG.Infof("Service started: <http://%s>", global.CFG.ListenAddr)
		if err := server.ListenAndServe(); errors.Is(err, http.ErrServerClosed) {
			global.LOG.Warn("Service shutting down")
		} else if err != nil {
			global.LOG.Fatal(err.Error())
		}
	}()

	osutil.WaitForInterrupt()

	shutdownCtx, cancel := context.WithTimeout(context.Background(), time.Second*5)
	defer cancel()
	if err := server.Shutdown(shutdownCtx); err != nil {
		global.LOG.Warn(err.Error())
	}

	if err := storage.Flush(); err != nil {
		global.LOG.Error(err.Error())
	}
}
