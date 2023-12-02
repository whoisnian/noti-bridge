package transporter

import (
	"bytes"
	"crypto"
	"crypto/aes"
	"crypto/cipher"
	"crypto/ecdh"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/hmac"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"math/big"
	"net/url"
	"os"
	"time"

	"github.com/whoisnian/noti-bridge/server/global"
)

var vapidCred struct {
	PublicKey  string
	PrivateKey string
	Subject    string // https://datatracker.ietf.org/doc/html/rfc8292#section-2.1
}

func generateVAPID(filename string) error {
	fi, err := os.Create(filename)
	if err != nil {
		return err
	}
	defer fi.Close()

	privKey, err := ecdh.P256().GenerateKey(rand.Reader)
	if err != nil {
		return err
	}
	vapidCred.PrivateKey = base64.RawURLEncoding.EncodeToString(privKey.Bytes())
	vapidCred.PublicKey = base64.RawURLEncoding.EncodeToString(privKey.PublicKey().Bytes())
	vapidCred.Subject = "https://github.com/whoisnian/noti-bridge"

	global.LOG.Debugf("VAPID.PrivateKey: %s", vapidCred.PrivateKey)
	global.LOG.Debugf("VAPID.PublicKey: %s", vapidCred.PublicKey)
	return json.NewEncoder(fi).Encode(vapidCred)
}

func vapidECDSA() (*ecdsa.PrivateKey, error) {
	publicBytes, err := base64.RawURLEncoding.DecodeString(vapidCred.PublicKey)
	if err != nil {
		return nil, err
	}
	privateBytes, err := base64.RawURLEncoding.DecodeString(vapidCred.PrivateKey)
	if err != nil {
		return nil, err
	}

	// https://github.com/golang/go/issues/63963
	return &ecdsa.PrivateKey{
		PublicKey: ecdsa.PublicKey{
			Curve: elliptic.P256(),
			X:     new(big.Int).SetBytes(publicBytes[1:33]),
			Y:     new(big.Int).SetBytes(publicBytes[33:]),
		},
		D: new(big.Int).SetBytes(privateBytes),
	}, nil
}

// Authorization: vapid t=[JWT Header].[JWT Payload].[Signature],k=[VAPID Public Key]
func vapidAuthHeader(endpoint string) (string, error) {
	// {"typ":"JWT","alg":"ES256"}
	const jwtHeader = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9"

	uri, err := url.Parse(endpoint)
	if err != nil {
		return "", err
	}

	payload, err := json.Marshal(map[string]any{
		"aud": uri.Scheme + "://" + uri.Host,
		"exp": time.Now().Add(time.Hour * 12).Unix(),
		"sub": vapidCred.Subject,
	})
	if err != nil {
		return "", err
	}
	jwtPayload := base64.RawURLEncoding.EncodeToString(payload)

	priv, err := vapidECDSA()
	if err != nil {
		return "", err
	}

	hasher := crypto.SHA256.New()
	hasher.Write([]byte(jwtHeader))
	hasher.Write([]byte{'.'})
	hasher.Write([]byte(jwtPayload))
	r, s, err := ecdsa.Sign(rand.Reader, priv, hasher.Sum(nil))
	if err != nil {
		return "", err
	}

	buf := make([]byte, 64)
	r.FillBytes(buf[:32])
	s.FillBytes(buf[32:])
	jwtSignature := base64.RawURLEncoding.EncodeToString(buf)
	return fmt.Sprintf(
		"vapid t=%s.%s.%s,k=%s",
		jwtHeader, jwtPayload, jwtSignature,
		vapidCred.PublicKey,
	), nil
}

// https://datatracker.ietf.org/doc/html/rfc8188
//
//	auth: ua_private_bytes
//	p256dh: ua_public_bytes
func vapidECE(payload []byte, auth []byte, p256dh []byte) (*bytes.Buffer, error) {
	// Salt
	salt := make([]byte, 16)
	if _, err := rand.Read(salt); err != nil {
		return nil, err
	}

	// User agent private key: ua_private
	uaPrivateBytes := auth
	// User agent public key: ua_public
	curve := ecdh.P256()
	uaPublicKey, err := curve.NewPublicKey(p256dh)
	if err != nil {
		return nil, err
	}

	// Application server private key: as_private
	asPrivateKey, err := curve.GenerateKey(rand.Reader)
	if err != nil {
		return nil, err
	}
	// Application server public key: as_public
	asPublicBytes := asPrivateKey.PublicKey().Bytes()

	// Shared ECDH secret: ecdh_secret = ECDH(as_private, ua_public)
	ecdhSecretBytes, err := asPrivateKey.ECDH(uaPublicKey)
	if err != nil {
		return nil, err
	}
	// Authentication secret: auth_secret = ua_private
	authSecretBytes := uaPrivateBytes

	// Pseudorandom key for key combining: PRK_key = HKDF-Extract(salt=auth_secret, IKM=ecdh_secret)
	// Info for key combining: key_info = "WebPush: info" || 0x00 || ua_public || as_public
	// Input keying material for content encryption key derivation: IKM = HKDF-Expand(PRK_key, key_info, L_key=32)
	keyInfo := make([]byte, 14+len(p256dh)+len(asPublicBytes))
	copy(keyInfo, []byte("WebPush: info\x00"))
	copy(keyInfo[14:], p256dh)
	copy(keyInfo[14+len(p256dh):], asPublicBytes)
	ikm := hkdf(authSecretBytes, ecdhSecretBytes, keyInfo, 32)

	// PRK for content encryption: PRK = HKDF-Extract(salt, IKM)
	prk := hkdfExtract(salt, ikm)
	// Info for content encryption key derivation: cek_info = "Content-Encoding: aes128gcm" || 0x00
	cekInfo := []byte("Content-Encoding: aes128gcm\x00")
	// Content encryption key: CEK = HKDF-Expand(PRK, cek_info, L_cek=16)
	cek := hkdfExpand(prk, cekInfo, 16)
	// Info for content encryption nonce derivation: nonce_info = "Content-Encoding: nonce" || 0x00
	nonceInfo := []byte("Content-Encoding: nonce\x00")
	// Nonce: NONCE = HKDF-Expand(PRK, nonce_info, L_nonce=12)
	nonce := hkdfExpand(prk, nonceInfo, 12)

	// https://datatracker.ietf.org/doc/html/rfc8291#section-4
	// MAX_BODY(4096) = FIXED_HEADER(86) + MAX_PAYLOAD(3993) + MIN_PADDING(1) + AEAD_AES_128_GCM_EXPANSION(16)
	// FIXED_HEADER(86) = salt(16) + rs(4) + idlen(1) + keyid(usually 65)
	bodyBuffer := bytes.NewBuffer(salt)
	bodyBuffer.Write([]byte{0x00, 0x00, 0x10, 0x00}) // binary.BigEndian.Uint32(4096)
	bodyBuffer.WriteByte(byte(len(asPublicBytes)))
	bodyBuffer.Write(asPublicBytes)

	// AEAD_AES_128_GCM
	cBlock, err := aes.NewCipher(cek)
	if err != nil {
		return nil, err
	}
	gcm, err := cipher.NewGCM(cBlock)
	if err != nil {
		return nil, err
	}
	payloadBuffer := bytes.NewBuffer(payload)
	payloadBuffer.WriteByte(0x02)
	payloadBuffer.Write(make([]byte, 4096-bodyBuffer.Len()-payloadBuffer.Len()-16))

	bodyBuffer.Write(gcm.Seal(nil, nonce, payloadBuffer.Bytes(), nil))
	return bodyBuffer, nil
}

func hkdfExtract(salt []byte, ikm []byte) []byte {
	h := hmac.New(sha256.New, salt)
	h.Write(ikm)
	return h.Sum(nil)
}

func hkdfExpand(prk []byte, info []byte, length int) []byte {
	h := hmac.New(sha256.New, prk)
	h.Write(info)
	h.Write([]byte{0x01})
	return h.Sum(nil)[:length]
}

// https://datatracker.ietf.org/doc/html/rfc5869
func hkdf(salt []byte, ikm []byte, info []byte, length int) []byte {
	prk := hkdfExtract(salt, ikm)
	return hkdfExpand(prk, info, length)
}
