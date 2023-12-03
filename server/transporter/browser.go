package transporter

import (
	"encoding/json"
	"io"
	"net/http"
	"os"
	"strconv"

	"github.com/whoisnian/noti-bridge/server/global"
	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/task"
)

func BrowserServerKey() string {
	return vapidCred.PublicKey
}

func SetupBrowser(filename string) error {
	fi, err := os.Open(filename)
	if os.IsNotExist(err) {
		global.LOG.Warnf("%s, generating new VAPID credential", err.Error())
		return generateVAPID(filename)
	} else if err != nil {
		return err
	}
	defer fi.Close()
	return json.NewDecoder(fi).Decode(&vapidCred)
}

func NotifyBrowser(tsk *task.Task, dev *storage.Device) error {
	payload, err := json.Marshal(tsk)
	if err != nil {
		return err
	}

	extra := storage.BrowserExtra{}
	if err := json.Unmarshal(dev.Extra, &extra); err != nil {
		return err
	}

	body, err := vapidECE(payload, extra.Auth, extra.P256dh)
	if err != nil {
		return err
	}

	authHeader, err := vapidAuthHeader(dev.Token)
	if err != nil {
		return err
	}

	req, err := http.NewRequest("POST", dev.Token, body)
	if err != nil {
		return err
	}
	req.Header.Set("Authorization", authHeader)
	req.Header.Set("Content-Encoding", "aes128gcm")
	req.Header.Set("Content-Length", strconv.Itoa(body.Len()))
	req.Header.Set("Content-Type", "application/octet-stream")
	req.Header.Set("TTL", "0")
	req.Header.Set("Urgency", "high")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	if global.CFG.Debug {
		res, err := io.ReadAll(resp.Body)
		global.LOG.Debugf("NotifyBrowser result: %s %v %s", resp.Status, err, string(res))
	}
	return resp.Body.Close()
}
