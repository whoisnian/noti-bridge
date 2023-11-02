package storage

import (
	"os"
	"path/filepath"
	"sync"
)

var (
	gGroupMap  = newGroupMap()
	gDeviceMap = newDeviceMap()
	gLocker    = new(sync.RWMutex)

	dataDir string
)

func SetupDataDir(dir string) error {
	wd, err := os.Getwd()
	if err != nil {
		return err
	}
	dataDir = filepath.Join(wd, dir)

	if err := os.MkdirAll(dataDir, 0755); err != nil {
		return err
	}

	gLocker.Lock()
	defer gLocker.Unlock()

	if err = gDeviceMap.loadFrom(filepath.Join(dataDir, gDeviceMap.fName())); err != nil {
		return err
	}
	return gGroupMap.loadFrom(filepath.Join(dataDir, gGroupMap.fName()))
}

func Flush() error {
	gLocker.Lock()
	defer gLocker.Unlock()

	if err := gDeviceMap.saveAs(filepath.Join(dataDir, gDeviceMap.fName())); err != nil {
		return err
	}
	return gGroupMap.saveAs(filepath.Join(dataDir, gGroupMap.fName()))
}
