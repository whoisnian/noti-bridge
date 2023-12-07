let DB = null
const TASKS = 'tasks'
const GROUPS = 'groups'
const KV = 'kv'
const openDB = async () => {
  if (DB != null) return DB
  return new Promise((resolve, reject) => {
    const openDBRequest = self.indexedDB.open('noti.db', 1)
    openDBRequest.onerror = reject
    openDBRequest.onsuccess = e => resolve(DB = e.target.result)
    openDBRequest.onupgradeneeded = e => {
      e.target.result.createObjectStore(TASKS, { autoIncrement: true })
      e.target.result.createObjectStore(GROUPS, { autoIncrement: true })
      e.target.result.createObjectStore(KV)
    }
  })
}

const dbAddOne = async (name, value) => {
  const db = await dbOpen()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction(name, 'readwrite')
      .objectStore(name)
      .add(value)
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

self.addEventListener('notificationclick', event => {
  console.log('notification clicked', event)
  event.notification.close()

  const data = event.notification.data
  if (event.action === 'open' && data.Type === 'link') {
    event.waitUntil(self.clients.openWindow(data.Link))
  } else if (data.Type === 'link') {
    event.waitUntil(self.clients.openWindow('/web'))
  }
})

self.addEventListener('push', event => {
  console.log('push message received', event)
  const data = event.data?.json()
  if (!data) return

  event.waitUntil(dbAddOne(TASKS, { ...data, CTime: (new Date()).getTime() }))

  if (!(self.Notification && self.Notification.permission === 'granted')) {
    console.error('missing notification permission in service worker')
    return
  }

  const { Type, Text, Link } = data
  let Title = data.Title ?? ''
  const options = { data }

  switch (Type) {
    case 'ping':
      Title = 'Ping'
      break
    case 'text':
      options.body = Text
      break
    case 'link':
      options.body = Link
      options.actions = [{ action: 'open', title: 'OPEN LINK' }]
      break
  }

  event.waitUntil(self.registration.showNotification(Title, options))
})
