package storage

import (
	"encoding/json"
	"os"

	"github.com/whoisnian/glb/util/osutil"
)

const (
	DeviceAndroid = iota
	DeviceBrowser
)

type Device struct {
	Type  int64
	Token string
	CTime int64
	ATime int64
	Name  string
	Extra json.RawMessage
}

type BrowserExtra struct {
	Auth   []byte
	P256dh []byte
}

type DeviceIndex struct {
	Type  int64
	Token string
}

func (d *Device) index() DeviceIndex {
	return DeviceIndex{d.Type, d.Token}
}

type DeviceMap struct {
	m map[DeviceIndex]*Device
}

func newDeviceMap() *DeviceMap {
	return &DeviceMap{make(map[DeviceIndex]*Device)}
}

func (*DeviceMap) fName() string {
	return "devices.jsonl"
}

func (dm *DeviceMap) loadFrom(fPath string) error {
	fi, err := os.OpenFile(fPath, os.O_RDONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	dec := json.NewDecoder(fi)
	for dec.More() {
		d := new(Device)
		if err := dec.Decode(d); err != nil {
			return err
		} else {
			dm.m[d.index()] = d
		}
	}
	return nil
}

func (dm *DeviceMap) saveAs(fPath string) error {
	fi, err := os.OpenFile(fPath, os.O_RDWR|os.O_CREATE|os.O_TRUNC, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	enc := json.NewEncoder(fi)
	for _, d := range dm.m {
		if err := enc.Encode(d); err != nil {
			return err
		}
	}
	return nil
}
