package storage

import (
	"encoding/json"
	"time"
)

func UpdateDevice(typ int64, token string, name string, extra json.RawMessage) error {
	gLocker.Lock()
	defer gLocker.Unlock()

	now := time.Now().Unix()
	idx := DeviceIndex{typ, token}
	if d, ok := gDeviceMap.m[idx]; !ok {
		gDeviceMap.m[idx] = &Device{typ, token, now, now, name, extra}
	} else {
		d.ATime = now
		d.Name = name
	}
	return nil
}

func DeleteDevice(typ int64, token string) error {
	gLocker.Lock()
	defer gLocker.Unlock()

	idx := DeviceIndex{typ, token}
	delete(gDeviceMap.m, idx)
	return nil
}

func Bind(gids []string, typ int64, token string, name string, extra json.RawMessage) error {
	gLocker.Lock()
	defer gLocker.Unlock()

	now := time.Now().Unix()
	idx := DeviceIndex{typ, token}
	if d, ok := gDeviceMap.m[idx]; !ok {
		gDeviceMap.m[idx] = &Device{typ, token, now, now, name, extra}
	} else {
		d.ATime = now
		d.Name = name
	}

bindLoop:
	for _, gid := range gids {
		if g, ok := gGroupMap.m[gid]; !ok {
			gGroupMap.m[gid] = &Group{gid, now, now, []DeviceIndex{idx}}
		} else {
			g.ATime = now
			for _, sub := range g.Subs {
				if sub == idx {
					continue bindLoop
				}
			}
			g.Subs = append(g.Subs, idx)
		}
	}
	return nil
}

func UnBind(gids []string, typ int64, token string) error {
	gLocker.Lock()
	defer gLocker.Unlock()

	now := time.Now().Unix()
	idx := DeviceIndex{typ, token}
	for _, gid := range gids {
		if g, ok := gGroupMap.m[gid]; ok {
			g.ATime = now
			for i, sub := range g.Subs {
				if sub == idx {
					g.Subs = append(g.Subs[:i], g.Subs[i+1:]...)
					break
				}
			}
		}
	}
	return nil
}

func List(gid string) []Device {
	gLocker.RLock()
	defer gLocker.RUnlock()

	now := time.Now().Unix()
	result := []Device{}
	if g, ok := gGroupMap.m[gid]; ok {
		g.ATime = now
		for _, sub := range g.Subs {
			if d, ok := gDeviceMap.m[sub]; ok {
				result = append(result, *d)
			}
		}
	}
	return result
}
