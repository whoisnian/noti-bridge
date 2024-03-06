package global

import "github.com/whoisnian/glb/config"

var CFG Config

type Config struct {
	Debug   bool   `flag:"d,false,Enable debug output"`
	LogFmt  string `flag:"log,nano,Log output format, one of nano, text and json"`
	Version bool   `flag:"v,false,Show version and quit"`

	ListenAddr    string `flag:"l,127.0.0.1:9000,Server listen addr"`
	StoragePath   string `flag:"s,.data,Server storage directory path"`
	FCMCredFile   string `flag:"fcm,.config/fcm.json,Firebase service account credential file"`
	VAPIDCredFile string `flag:"vapid,.config/vapid.json,Web Push VAPID keys credential file"`
}

func SetupConfig() {
	err := config.FromCommandLine(&CFG)
	if err != nil {
		panic(err)
	}
}
