package storage

import (
	"encoding/json"
	"os"

	"github.com/whoisnian/glb/util/osutil"
)

type Group struct {
	GID   string
	CTime int64
	ATime int64
	Subs  []DeviceIndex
}

type GroupMap struct {
	m map[string]*Group
}

func newGroupMap() *GroupMap {
	return &GroupMap{make(map[string]*Group)}
}

func (*GroupMap) fName() string {
	return "groups.json"
}

func (gm *GroupMap) loadFrom(fPath string) error {
	fi, err := os.OpenFile(fPath, os.O_RDONLY|os.O_CREATE, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	dec := json.NewDecoder(fi)
	for dec.More() {
		g := new(Group)
		if err := dec.Decode(g); err != nil {
			return err
		} else {
			gm.m[g.GID] = g
		}
	}
	return nil
}

func (gm *GroupMap) saveAs(fPath string) error {
	fi, err := os.OpenFile(fPath, os.O_RDWR|os.O_CREATE|os.O_TRUNC, osutil.DefaultFileMode)
	if err != nil {
		return err
	}
	defer fi.Close()

	enc := json.NewEncoder(fi)
	for _, g := range gm.m {
		if err := enc.Encode(g); err != nil {
			return err
		}
	}
	return nil
}
