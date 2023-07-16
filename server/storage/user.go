package storage

import (
	"encoding/json"
	"os"
	"time"

	"github.com/whoisnian/glb/util/osutil"
)

type User struct {
	UID   string
	CTime time.Time
	ATime time.Time
}

type UserMap map[string]*User

func (UserMap) fName() string {
	return "users.json"
}

func (m UserMap) loadFrom(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_RDONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	dec := json.NewDecoder(fi)
	for dec.More() {
		u := new(User)
		if err := dec.Decode(u); err != nil {
			finerr = err
		} else {
			m[u.UID] = u
		}
	}
	return finerr
}

func (m UserMap) saveAs(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_WRONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	enc := json.NewEncoder(fi)
	for _, u := range m {
		if err := enc.Encode(u); err != nil {
			finerr = err
		}
	}
	return finerr
}
