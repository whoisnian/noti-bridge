# server

## storage
### group
gid ctime atime subs
* uniq(gid)

### device
type token ctime name
* uniq(type, token)

## api
* bind: gid, type, token, name => ok/err
  ```sh
  curl -X PUT \
    -H 'Content-Type: application/json' \
    -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Type":0,"Token":"e9KDLKiLTKiRAm1EAj4J_H:APA91bEwwCcUtPxeLu_Dck1FnmvaOyrNxnwEInDOuU1HWQQbVCHFvnhi_hcJrhe5h8IuFAQrbtKuM04ZBLM9dY-n6P1U7rttGs9wweXuerL2ZBoqx969NPqK6LTX-F-UZIr9DT4tpret","Name":"test"}' \
    '127.0.0.1:9000/api/device'
  ```
* unbind: gid, type, token => ok/err
  ```sh
  curl -X DELETE \
    -H 'Content-Type: application/json' \
    -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Type":0,"Token":"e9KDLKiLTKiRAm1EAj4J_H:APA91bEwwCcUtPxeLu_Dck1FnmvaOyrNxnwEInDOuU1HWQQbVCHFvnhi_hcJrhe5h8IuFAQrbtKuM04ZBLM9dY-n6P1U7rttGs9wweXuerL2ZBoqx969NPqK6LTX-F-UZIr9DT4tpret"}' \
    '127.0.0.1:9000/api/device'
  ```
* list: gid => []devices{type, token, name}
  ```sh
  curl '127.0.0.1:9000/api/device?GID=f50241fcc0f506b6bed770c72bd01ebb'
  ```
* message: gid, task => ok/err
  ```sh
  curl -X POST \
    -H 'Content-Type: application/json' \
    -d '{"GID":"f50241fcc0f506b6bed770c72bd01ebb","Task":{"Type":"TypePing"}}' \
    '127.0.0.1:9000/api/task'
  ```
