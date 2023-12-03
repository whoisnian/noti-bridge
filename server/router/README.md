# router <!-- omit in toc -->
- [page](#page)
  - [web-push](#web-push)
  - [static](#static)
  - [server-key](#server-key)
- [api](#api)
  - [updateDevice](#updatedevice)
  - [deleteDevice](#deletedevice)
  - [bindGroups](#bindgroups)
  - [unbindGroups](#unbindgroups)
  - [task](#task)

## page
### web-push
`GET /web`
### static
`GET /static/*`
### server-key
`GET /server-key`

## api
### updateDevice
params: type, token, name, extra
```sh
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf","Name":"test"}' \
  '127.0.0.1:9000/api/device'

curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"Type":1,"Token":"https://fcm.googleapis.com/fcm/send/d_8dUaGOmtI:APA91bHnNdY71-ToIlzujomfENQWktsYFsMAcNUndikA4I__2IhjjClNL-Hzib4c70MuP5_ebtDc9x_R4QXGY53gMBu0JTCnZa5f31RIu0tGDr6LWttKP5yVNFYR7w4tcqUs5oUdNiNO","Name":"Chrome/119","Extra":{"Auth":"8LGEXbYEecyXG2bFHQpk3Q==","P256dh":"BCuMHwrhuTko0j/89uFDbtr+RjzkfEZ+lmPIXcnc0k+Zp32YXrQTQmq9av0oGF/PX9Fb3KxwS70Kkj5v+qE9iq4="}}' \
  '127.0.0.1:9000/api/device'
```

### deleteDevice
params: type, token
```sh
curl -X DELETE \
  -H 'Content-Type: application/json' \
  -d '{"Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf"}' \
  '127.0.0.1:9000/api/device'
```

### bindGroups
params: gids, type, token, name, extra
```sh
curl -X PUT \
  -H 'Content-Type: application/json' \
  -d '{"GIDs":["f50241fcc0f506b6bed770c72bd01ebb"],"Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf","Name":"test"}' \
  '127.0.0.1:9000/api/device'
```

### unbindGroups
params: gids, type, token
```sh
curl -X DELETE \
  -H 'Content-Type: application/json' \
  -d '{"GIDs":"f50241fcc0f506b6bed770c72bd01ebb","Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf"}' \
  '127.0.0.1:9000/api/device'
```

### task
params: gid, task
```sh
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Task":{"Type":"ping"}}' \
  '127.0.0.1:9000/api/task'

curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Task":{"Type":"link","Title":"github repo","Link":"https://github.com/whoisnian/noti-bridge"}}' \
  '127.0.0.1:9000/api/task'
```
