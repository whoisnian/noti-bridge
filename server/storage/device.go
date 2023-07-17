package storage

import (
	"encoding/json"
	"os"
	"sync"
	"time"

	"github.com/whoisnian/glb/util/osutil"
)

const (
	DeviceAndroid = iota
)

type Device struct {
	UID   string
	Name  string
	Type  int64
	Token string
	CTime time.Time
}

type DeviceIndex struct {
	Type  int64
	Token string
}

func (d *Device) index() DeviceIndex {
	return DeviceIndex{d.Type, d.Token}
}

type DeviceMap struct {
	m      map[DeviceIndex]*Device
	locker sync.RWMutex
}

func newDeviceMap() *DeviceMap {
	return &DeviceMap{
		m: make(map[DeviceIndex]*Device),
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
			m.m[d.index()] = d
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
	for _, d := range m.m {
		if err := enc.Encode(d); err != nil {
			return err
		}
	}

	return finerr
}

func (m *DeviceMap) Find(typ int64, token string) *Device {
	m.locker.RLock()
	defer m.locker.RUnlock()
	return m.m[DeviceIndex{typ, token}]
}

func (m *DeviceMap) Create(uid string, name string, typ int64, token string, ctime time.Time) {
	m.locker.Lock()
	defer m.locker.Unlock()
	m.m[DeviceIndex{typ, token}] = &Device{uid, name, typ, token, ctime}
}

func (m *DeviceMap) Update(uid string, name string, typ int64, token string, ctime time.Time) {
	m.locker.Lock()
	defer m.locker.Unlock()
	d := m.m[DeviceIndex{typ, token}]
	d.UID = uid
	d.Name = name
	d.CTime = ctime
}

func (m *DeviceMap) Delete(typ int64, token string) {
	m.locker.Lock()
	defer m.locker.Unlock()
	delete(m.m, DeviceIndex{typ, token})
}
