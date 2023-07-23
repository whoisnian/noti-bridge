package storage

import "time"

func Bind(gid string, typ int64, token string, name string) error {
	gLocker.Lock()
	defer gLocker.Unlock()

	now := time.Now().Unix()
	idx := DeviceIndex{typ, token}
	if d, ok := gDeviceMap.m[idx]; !ok {
		gDeviceMap.m[idx] = &Device{typ, token, now, name}
	} else {
		d.Name = name
	}

	if g, ok := gGroupMap.m[gid]; !ok {
		g = &Group{gid, now, now, []DeviceIndex{idx}}
		gGroupMap.m[gid] = g
	} else {
		g.ATime = now
		for _, sub := range g.Subs {
			if sub == idx {
				return nil
			}
		}
		g.Subs = append(g.Subs, idx)
	}
	return nil
}

func UnBind(gid string, typ int64, token string) error {
	gLocker.Lock()
	defer gLocker.Unlock()

	idx := DeviceIndex{typ, token}
	if g, ok := gGroupMap.m[gid]; ok {
		for i, sub := range g.Subs {
			if sub == idx {
				g.Subs = append(g.Subs[:i], g.Subs[i+1:]...)
				return nil
			}
		}
	}
	return nil
}

func List(gid string) []Device {
	gLocker.RLock()
	defer gLocker.RUnlock()

	result := []Device{}
	if g, ok := gGroupMap.m[gid]; ok {
		for _, sub := range g.Subs {
			if d, ok := gDeviceMap.m[sub]; ok {
				result = append(result, *d)
			}
		}
	}
	return result
}
