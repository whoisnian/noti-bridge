package global

import (
	"os"

	"github.com/whoisnian/glb/ansi"
	"github.com/whoisnian/glb/config"
	"github.com/whoisnian/glb/logger"
)

type Config struct {
	Debug          bool   `flag:"d,false,Enable debug output"`
	ListenAddr     string `flag:"l,127.0.0.1:9000,Server listen addr"`
	DataPath       string `flag:"d,.data,Server data directory"`
	CredentialPath string `flag:"c,.config/fcm.json,Firebase service account credential file"`
	Version        bool   `flag:"v,false,Show version and quit"`
}

var (
	CFG Config
	LOG *logger.Logger

	AppName   = "noti-bridge"
	Version   = "unknown"
	BuildTime = "unknown"
)

func Setup() {
	err := config.FromCommandLine(&CFG)
	if err != nil {
		panic(err)
	}

	if CFG.Debug {
		LOG = logger.New(logger.NewNanoHandler(os.Stderr, logger.NewOptions(
			logger.LevelDebug, ansi.IsSupported(os.Stderr.Fd()), false,
		)))
	} else {
		LOG = logger.New(logger.NewTextHandler(os.Stderr, logger.NewOptions(
			logger.LevelInfo, false, true,
		)))
	}
}
