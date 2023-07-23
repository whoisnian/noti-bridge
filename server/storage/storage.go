package storage

import (
	"log"
	"os"
	"path/filepath"
)

var (
	gGroupMap  = newGroupMap()
	gDeviceMap = newDeviceMap()

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

	if err = gGroupMap.loadFrom(filepath.Join(dataDir, gGroupMap.fName())); err != nil {
		log.Fatalln(err)
	}
	if err = gDeviceMap.loadFrom(filepath.Join(dataDir, gDeviceMap.fName())); err != nil {
		log.Fatalln(err)
	}
}

func Flush() error {
	if err := gGroupMap.saveAs(filepath.Join(dataDir, gGroupMap.fName())); err != nil {
		return err
	}
	return gDeviceMap.saveAs(filepath.Join(dataDir, gDeviceMap.fName()))
}
