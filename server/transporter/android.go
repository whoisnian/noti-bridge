package transporter

import (
	"context"
	"log"
	"time"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"github.com/whoisnian/noti-bridge/task"
	"google.golang.org/api/option"
)

var (
	fcmClient *messaging.Client

	zeroTTL = new(time.Duration)
)

func SetupAndroid(filename string) {
	opt := option.WithCredentialsFile(filename)
	app, err := firebase.NewApp(context.Background(), nil, opt)
	if err != nil {
		log.Fatalln(err)
	}

	fcmClient, err = app.Messaging(context.Background())
	if err != nil {
		log.Fatalln(err)
	}
}

func NotifyAndroid(tsk *task.Task, token string) error {
	msg := &messaging.Message{
		Token: token,
		Android: &messaging.AndroidConfig{
			Priority: "high",
			TTL:      zeroTTL,
			Data:     tsk.ToMap(),
		},
	}

	res, err := fcmClient.Send(context.Background(), msg)
	log.Println(res)
	return err
}
