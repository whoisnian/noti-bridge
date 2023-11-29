package transporter

import (
	"crypto/ecdh"
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"os"

	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/task"
)

var VAPID struct {
	PublicKey  string
	PrivateKey string
}

func SetupBrowser(filename string) error {
	fi, err := os.Open(filename)
	if os.IsNotExist(err) {
		global.LOG.Warnf("%s, generating new VAPID", err.Error())
		return generateVAPID(filename)
	} else if err != nil {
		return err
	}
	defer fi.Close()
	return json.NewDecoder(fi).Decode(&VAPID)
}

func generateVAPID(filename string) error {
	fi, err := os.Create(filename)
	if err != nil {
		return err
	}
	defer fi.Close()

	curve := ecdh.P256()
	pKey, err := curve.GenerateKey(rand.Reader)
	if err != nil {
		return err
	}
	VAPID.PrivateKey = base64.RawURLEncoding.EncodeToString(pKey.Bytes())
	global.LOG.Infof("VAPID.PrivateKey: %s", VAPID.PrivateKey)
	VAPID.PublicKey = base64.RawURLEncoding.EncodeToString(pKey.PublicKey().Bytes())
	global.LOG.Infof("VAPID.PublicKey: %s", VAPID.PublicKey)
	return json.NewEncoder(fi).Encode(VAPID)
}

func NotifyBrowser(tsk *task.Task, dev *storage.Device) error {
	return nil
}
