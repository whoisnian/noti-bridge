package transporter

import (
	"errors"

	"github.com/whoisnian/noti-bridge/server/storage"
	"github.com/whoisnian/noti-bridge/server/task"
)

func Notify(tsk *task.Task, dev *storage.Device) error {
	switch dev.Type {
	case storage.DeviceAndroid:
		return NotifyAndroid(tsk, dev)
	case storage.DeviceBrowser:
		return NotifyBrowser(tsk, dev)
	default:
		return errors.New("unknown device type")
	}
}
