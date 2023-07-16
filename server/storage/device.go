package storage

import (
	"encoding/json"
	"os"
	"sync"
	"time"

	"github.com/whoisnian/glb/util/osutil"
)

type Device struct {
	UID   string
	Name  string
	Type  int64
	Token string
	CTime time.Time
}

type DeviceMap struct {
	m      map[string][]*Device
	locker sync.RWMutex
}

func newDeviceMap() *DeviceMap {
	return &DeviceMap{
		m: make(map[string][]*Device),
	}
}

func (*DeviceMap) fName() string {
	return "devices.json"
}

func (m *DeviceMap) loadFrom(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_RDONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	m.locker.Lock()
	defer m.locker.Unlock()

	dec := json.NewDecoder(fi)
	for dec.More() {
		d := new(Device)
		if err := dec.Decode(d); err != nil {
			return err
		} else {
			m.m[d.UID] = append(m.m[d.UID], d)
		}
	}
	return finerr
}

func (m *DeviceMap) saveAs(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_WRONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	m.locker.RLock()
	defer m.locker.RUnlock()

	enc := json.NewEncoder(fi)
	for _, dl := range m.m {
		for _, d := range dl {
			if err := enc.Encode(d); err != nil {
				return err
			}
		}
	}
	return finerr
}
