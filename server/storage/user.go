package storage

import (
	"encoding/json"
	"os"
	"sync"
	"time"

	"github.com/whoisnian/glb/util/osutil"
)

type User struct {
	UID   string
	CTime time.Time
	ATime time.Time
}

type UserMap struct {
	m      map[string]*User
	locker sync.RWMutex
}

func newUserMap() *UserMap {
	return &UserMap{
		m: make(map[string]*User),
	}
}

func (*UserMap) fName() string {
	return "users.json"
}

func (m *UserMap) loadFrom(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_RDONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	m.locker.Lock()
	defer m.locker.Unlock()

	dec := json.NewDecoder(fi)
	for dec.More() {
		u := new(User)
		if err := dec.Decode(u); err != nil {
			return err
		} else {
			m.m[u.UID] = u
		}
	}
	return finerr
}

func (m *UserMap) saveAs(fPath string) (finerr error) {
	fi, err := os.OpenFile(fPath, os.O_WRONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	m.locker.RLock()
	defer m.locker.RUnlock()

	enc := json.NewEncoder(fi)
	for _, u := range m.m {
		if err := enc.Encode(u); err != nil {
			return err
		}
	}
	return finerr
}

func (m *UserMap) Find(uid string) *User {
	m.locker.RLock()
	defer m.locker.RUnlock()
	return m.m[uid]
}

func (m *UserMap) Create(uid string, ctime time.Time, atime time.Time) {
	m.locker.Lock()
	defer m.locker.Unlock()
	m.m[uid] = &User{uid, ctime, atime}
}

func (m *UserMap) Update(uid string, ctime time.Time, atime time.Time) {
	m.locker.Lock()
	defer m.locker.Unlock()
	u := m.m[uid]
	u.CTime = ctime
	u.ATime = atime
}

func (m *UserMap) Delete(uid string) {
	m.locker.Lock()
	defer m.locker.Unlock()
	delete(m.m, uid)
}
