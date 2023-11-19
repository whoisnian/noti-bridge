# api

## updateDevice
params: type, token, name
```sh
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf","Name":"test"}' \
  '127.0.0.1:9000/api/device'
```

## deleteDevice
params: type, token
```sh
curl -X DELETE \
  -H 'Content-Type: application/json' \
  -d '{"Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf"}' \
  '127.0.0.1:9000/api/device'
```

## bindGroups
params: gids, type, token, name
```sh
curl -X PUT \
  -H 'Content-Type: application/json' \
  -d '{"GIDs":["f50241fcc0f506b6bed770c72bd01ebb"],"Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf","Name":"test"}' \
  '127.0.0.1:9000/api/device'
```

## unbindGroups
params: gids, type, token
```sh
curl -X DELETE \
  -H 'Content-Type: application/json' \
  -d '{"GIDs":"f50241fcc0f506b6bed770c72bd01ebb","Type":0,"Token":"ewSNx5l5TJOBLhE5cK9r1w:APA91bHxovPcitHn-8Qap6O6hv1jSYA_ZHdwV7t_TGm3Xomb1xHB6Z5zckNSKl87dpqAw0p0xEcucClEuEzUiPwZUHwLZSVX97fZ2owPGCK4e_BFgnO8wszFXJIJ3daYQxOj3_kFjCSf"}' \
  '127.0.0.1:9000/api/device'
```

## task
params: gid, task
```sh
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Task":{"Type":"ping"}}' \
  '127.0.0.1:9000/api/task'
```
