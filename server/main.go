package main

import (
	"log"

	"github.com/whoisnian/glb/config"
	"github.com/whoisnian/glb/util/osutil"
	"github.com/whoisnian/noti-bridge/global"
	"github.com/whoisnian/noti-bridge/storage"
	"github.com/whoisnian/noti-bridge/task"
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

	err = transporter.NotifyAndroid(&task.Task{Type: task.TypePing}, "e9KDLKiLTKiRAm1EAj4J_H:APA91bEwwCcUtPxeLu_Dck1FnmvaOyrNxnwEInDOuU1HWQQbVCHFvnhi_hcJrhe5h8IuFAQrbtKuM04ZBLM9dY-n6P1U7rttGs9wweXuerL2ZBoqx969NPqK6LTX-F-UZIr9DT4tpret")
	if err != nil {
		log.Fatalln(err)
	}

	osutil.WaitForInterrupt()

	if err = storage.Flush(); err != nil {
		log.Fatalln(err)
	}
}
