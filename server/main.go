package main

import (
	"context"
	"log"
	"time"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"google.golang.org/api/option"
)

func main() {
	ctx := context.Background()
	opt := option.WithCredentialsFile("service-account.json")

	app, err := firebase.NewApp(ctx, nil, opt)
	if err != nil {
		log.Fatalln(err)
	}

	client, err := app.Messaging(ctx)
	if err != nil {
		log.Fatalln(err)
	}

	registrationToken := "YOUR_REGISTRATION_TOKEN"
	ttl := time.Duration(0)
	message := &messaging.Message{
		Android: &messaging.AndroidConfig{
			Priority:              "high",
			TTL:                   &ttl,
			RestrictedPackageName: "com.whoisnian.noti",
			Data: map[string]string{
				"server": "noti-bridge",
			},
			Notification: &messaging.AndroidNotification{
				Title:    "test",
				Body:     "send from golang server",
				Priority: messaging.PriorityHigh,
			},
		},
		Token: registrationToken,
	}

	response, err := client.Send(ctx, message)
	if err != nil {
		log.Fatalln(err)
	}
	log.Println("success:", response)
}
