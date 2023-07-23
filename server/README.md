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
* unbind: gid, type, token => ok/err
* list: gid => []devices{type, token, name}
* message: gid, content => ok/err
