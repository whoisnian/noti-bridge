package main

import (
	"log"

	"github.com/whoisnian/noti-bridge/task"
	"github.com/whoisnian/noti-bridge/transporter"
)

func main() {
	transporter.SetupAndroid("service-account.json")

	err := transporter.NotifyAndroid(&task.Task{Type: task.TypePing}, "e9KDLKiLTKiRAm1EAj4J_H:APA91bEwwCcUtPxeLu_Dck1FnmvaOyrNxnwEInDOuU1HWQQbVCHFvnhi_hcJrhe5h8IuFAQrbtKuM04ZBLM9dY-n6P1U7rttGs9wweXuerL2ZBoqx969NPqK6LTX-F-UZIr9DT4tpret")
	if err != nil {
		log.Fatalln(err)
	}
}
