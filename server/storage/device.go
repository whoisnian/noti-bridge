package storage

import (
	"encoding/json"
	"os"
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

type DeviceMap map[string][]*Device

func (DeviceMap) fName() string {
	return "devices.json"
}

func (m DeviceMap) loadFrom(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_RDONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	dec := json.NewDecoder(fi)
	for dec.More() {
		d := new(Device)
		if err := dec.Decode(d); err != nil {
			finerr = err
		} else {
			m[d.UID] = append(m[d.UID], d)
		}
	}
	return finerr
}

func (m DeviceMap) saveAs(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_WRONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	enc := json.NewEncoder(fi)
	for _, dl := range m {
		for _, d := range dl {
			if err := enc.Encode(d); err != nil {
				finerr = err
			}
		}
	}
	return finerr
}
