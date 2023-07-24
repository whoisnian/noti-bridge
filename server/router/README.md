# api

## bind
params: gid, type, token, name
```sh
curl -X PUT \
  -H 'Content-Type: application/json' \
  -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Type":0,"Token":"e9KDLKiLTKiRAm1EAj4J_H:APA91bEwwCcUtPxeLu_Dck1FnmvaOyrNxnwEInDOuU1HWQQbVCHFvnhi_hcJrhe5h8IuFAQrbtKuM04ZBLM9dY-n6P1U7rttGs9wweXuerL2ZBoqx969NPqK6LTX-F-UZIr9DT4tpret","Name":"test"}' \
  '127.0.0.1:9000/api/device'
```

## unbind
params: gid, type, token
```sh
curl -X DELETE \
  -H 'Content-Type: application/json' \
  -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Type":0,"Token":"e9KDLKiLTKiRAm1EAj4J_H:APA91bEwwCcUtPxeLu_Dck1FnmvaOyrNxnwEInDOuU1HWQQbVCHFvnhi_hcJrhe5h8IuFAQrbtKuM04ZBLM9dY-n6P1U7rttGs9wweXuerL2ZBoqx969NPqK6LTX-F-UZIr9DT4tpret"}' \
  '127.0.0.1:9000/api/device'
```

## list
params: gid
```sh
curl '127.0.0.1:9000/api/device?GID=f50241fcc0f506b6bed770c72bd01ebb'
```

## message
params: gid, task
```sh
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Task":{"Type":"TypePing"}}' \
  '127.0.0.1:9000/api/task'
```
