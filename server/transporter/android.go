package transporter

import (
	"context"
	"time"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/task"
	"google.golang.org/api/option"
)

var (
	fcmClient *messaging.Client

	zeroTTL = new(time.Duration)
)

func SetupAndroid(filename string) error {
	opt := option.WithCredentialsFile(filename)
	app, err := firebase.NewApp(context.Background(), nil, opt)
	if err != nil {
		return err
	}

	fcmClient, err = app.Messaging(context.Background())
	return err
}

func NotifyAndroid(tsk *task.Task, dev *storage.Device) error {
	msg := &messaging.Message{
		Token: dev.Token,
		Android: &messaging.AndroidConfig{
			Priority: "high",
			TTL:      zeroTTL,
			Data:     tsk.ToMap(),
		},
	}

	result, err := fcmClient.Send(context.Background(), msg)
	global.LOG.Debugf("NotifyAndroid result: %s", result)
	return err
}
