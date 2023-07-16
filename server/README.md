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
* register: => user
* bind: uid, name, type, token => ok/err
* unbind: uid, type, token => ok/err
* list: uid => devices
* message: uid, content => ok/err
