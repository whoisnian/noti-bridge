# server

## storage
### user
uid ctime atime
* uniq(uid)

### device
uid name type token ctime
* index(uid)
* uniq(type, token)

## api
* bind: uid, name, type, token
* unbind: uid, type, token
* list: uid
