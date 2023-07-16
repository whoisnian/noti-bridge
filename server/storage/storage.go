package storage

import (
	"log"
	"os"
	"path/filepath"
)

var (
	gUserMap   = make(UserMap)
	gDeviceMap = make(DeviceMap)

	dataDir string
)

func SetupDataDir(dir string) {
	wd, err := os.Getwd()
	if err != nil {
		log.Fatalln(err)
	}

	dataDir = filepath.Join(wd, dir)
	if err = os.MkdirAll(dataDir, 0755); err != nil {
		log.Fatalln(err)
	}

	if err = gUserMap.loadFrom(filepath.Join(dataDir, gUserMap.fName())); err != nil {
		log.Fatalln(err)
	}
	if err = gDeviceMap.loadFrom(filepath.Join(dataDir, gDeviceMap.fName())); err != nil {
		log.Fatalln(err)
	}
}

func Flush() error {
	if err := gUserMap.saveAs(filepath.Join(dataDir, gUserMap.fName())); err != nil {
		return err
	}
	return gDeviceMap.saveAs(filepath.Join(dataDir, gDeviceMap.fName()))
}
