package main

import (
	"context"
	"errors"
	"log"
	"net/http"

	"github.com/whoisnian/glb/config"
	"github.com/whoisnian/glb/logger"
	"github.com/whoisnian/glb/util/osutil"
	"github.com/whoisnian/noti-bridge/global"
	"github.com/whoisnian/noti-bridge/router"
	"github.com/whoisnian/noti-bridge/storage"
	"github.com/whoisnian/noti-bridge/transporter"
)

func main() {
	err := config.FromCommandLine(&global.CFG)
	if err != nil {
		log.Fatalln(err)
	}

	if global.CFG.Version {
		log.Printf("%s %s(%s)\n", global.AppName, global.Version, global.BuildTime)
		return
	}

	storage.SetupDataDir(global.CFG.DataPath)
	transporter.SetupAndroid(global.CFG.CredentialPath)

	mux := router.Setup()
	server := &http.Server{Addr: global.CFG.ListenAddr, Handler: logger.Req(logger.Recovery(mux))}
	go func() {
		log.Print("Service started: <http://", global.CFG.ListenAddr, ">\n")
		if err := server.ListenAndServe(); errors.Is(err, http.ErrServerClosed) {
			log.Println("Service shutting down")
		} else if err != nil {
			log.Fatalln(err)
		}
	}()

	osutil.WaitForInterrupt()

	if err = server.Shutdown(context.Background()); err != nil {
		log.Println(err)
	}
	if err = storage.Flush(); err != nil {
		log.Fatalln(err)
	}
}
