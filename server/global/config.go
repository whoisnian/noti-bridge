package global

type Config struct {
	ListenAddr     string `flag:"l,127.0.0.1:9000,Server listen addr"`
	DataPath       string `flag:"d,.data,Server data directory"`
	CredentialPath string `flag:"c,.config/fcm.json,Firebase service account credential file"`
	Version        bool   `flag:"v,false,Show version and quit"`
}

var CFG Config
