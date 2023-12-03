let DB = null
const openDB = async () => {
  if (DB != null) return DB
  return new Promise((resolve, reject) => {
    const openDBRequest = self.indexedDB.open('noti.db', 1)
    openDBRequest.onerror = reject
    openDBRequest.onsuccess = e => resolve(DB = e.target.result)
    openDBRequest.onupgradeneeded = e => e.target.result.createObjectStore('tasks', { autoIncrement: true })
  })
}

const addTask = async (data) => {
  const db = await openDB()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction('tasks', 'readwrite')
      .objectStore('tasks')
      .add({ ...data, CTime: (new Date()).getTime() })
    request.onerror = reject
    request.onsuccess = e => resolve(e.result)
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

  event.waitUntil(addTask(data))

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
