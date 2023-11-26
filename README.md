# noti-bridge

## Android preview
<img src="doc/assets/android_main.webp" alt="android_main.webp" width="240"/> <img src="doc/assets/android_settings.webp" alt="android_settings.webp" width="240"/>

## FCM configuration
1. Sign up for a [Firebase account](https://console.firebase.google.com)
2. Create a Firebase project in the Firebase console
3. Generate a private key in [Service Accounts](https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk)
4. Download the key file and run server with `-c myapp-firebase-adminsdk-xxxxx-xxxxxxxxxx.json`

## VAPID keys
### Generated on first server run
```log
2023-11-26 22:20:34 [W] open .config/vapid.json: no such file or directory, generating new VAPID
2023-11-26 22:20:34 [I] VAPID.PrivateKey: _N_r9UdzDV4Pfzn7DlvPcaspO4eq5EZcmRsF6JC3BXc
2023-11-26 22:20:34 [I] VAPID.PublicKey: BFVlLgnrYhU9HczI4u7plC8ePO9nNoyoPYy-WSySlE5htkeApjh7G-SaLB2HEtpT4oZf0HAjV0m5gT4hZQIK8oM
```

### Generating manually with openssl
Reference: [github.com/web-push-libs/web-push-php](https://github.com/web-push-libs/web-push-php/blob/master/README.md#authentication-vapid)
```sh
########## 1.generate ##########
openssl ecparam -genkey -name prime256v1 -out private_ec_key.pem
# -----BEGIN EC PRIVATE KEY-----
# MHcCAQEEIAYvIZ/A0X9g4pQNnl2G+OO4mirct1hMB/Vl61sOBj6roAoGCCqGSM49
# AwEHoUQDQgAEbuzDaw1qkIc0CA1Lfa/rtKUbiqNXrP/qCS9ghTKUat4qnnQdTfIS
# rEI1svSlVFbrBvE8BCTWuu3fnFuZW2zUQA==
# -----END EC PRIVATE KEY-----

########## 2.check ##########
openssl ec -in private_ec_key.pem -text -noout
# read EC key
# Private-Key: (256 bit)
# priv:
#     06:2f:21:9f:c0:d1:7f:60:e2:94:0d:9e:5d:86:f8:
#     e3:b8:9a:2a:dc:b7:58:4c:07:f5:65:eb:5b:0e:06:
#     3e:ab
# pub:
#     04:6e:ec:c3:6b:0d:6a:90:87:34:08:0d:4b:7d:af:
#     eb:b4:a5:1b:8a:a3:57:ac:ff:ea:09:2f:60:85:32:
#     94:6a:de:2a:9e:74:1d:4d:f2:12:ac:42:35:b2:f4:
#     a5:54:56:eb:06:f1:3c:04:24:d6:ba:ed:df:9c:5b:
#     99:5b:6c:d4:40
# ASN1 OID: prime256v1
# NIST CURVE: P-256

openssl asn1parse -in private_ec_key.pem -dump
#     0:d=0  hl=2 l= 119 cons: SEQUENCE          
#     2:d=1  hl=2 l=   1 prim: INTEGER           :01
#     5:d=1  hl=2 l=  32 prim: OCTET STRING      
#       0000 - 06 2f 21 9f c0 d1 7f 60-e2 94 0d 9e 5d 86 f8 e3   ./!....`....]...
#       0010 - b8 9a 2a dc b7 58 4c 07-f5 65 eb 5b 0e 06 3e ab   ..*..XL..e.[..>.
#    39:d=1  hl=2 l=  10 cons: cont [ 0 ]        
#    41:d=2  hl=2 l=   8 prim: OBJECT            :prime256v1
#    51:d=1  hl=2 l=  68 cons: cont [ 1 ]        
#    53:d=2  hl=2 l=  66 prim: BIT STRING        
#       0000 - 00 04 6e ec c3 6b 0d 6a-90 87 34 08 0d 4b 7d af   ..n..k.j..4..K}.
#       0010 - eb b4 a5 1b 8a a3 57 ac-ff ea 09 2f 60 85 32 94   ......W..../`.2.
#       0020 - 6a de 2a 9e 74 1d 4d f2-12 ac 42 35 b2 f4 a5 54   j.*.t.M...B5...T
#       0030 - 56 eb 06 f1 3c 04 24 d6-ba ed df 9c 5b 99 5b 6c   V...<.$.....[.[l
#       0040 - d4 40                                             .@

########## 3.format ##########
openssl ec -in private_ec_key.pem -pubout -outform DER|tail -c 65|base64 -w 0|tr -d '='|tr '+/' '-_' > public_key.txt
# BG7sw2sNapCHNAgNS32v67SlG4qjV6z_6gkvYIUylGreKp50HU3yEqxCNbL0pVRW6wbxPAQk1rrt35xbmVts1EA

openssl ec -in private_ec_key.pem -no_public -outform DER|tail -c +8|head -c 32|base64 -w 0|tr -d '='|tr '+/' '-_' > private_key.txt
# Bi8hn8DRf2DilA2eXYb447iaKty3WEwH9WXrWw4GPqs
```
