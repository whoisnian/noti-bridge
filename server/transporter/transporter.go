package transporter

import (
	"errors"

	"github.com/whoisnian/noti-bridge/storage"
	"github.com/whoisnian/noti-bridge/task"
)

func Notify(tsk *task.Task, dev *storage.Device) error {
	switch dev.Type {
	case storage.DeviceAndroid:
		return NotifyAndroid(tsk, dev.Token)
	default:
		return errors.New("unknown device type")
	}
}
