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
  if (!(self.Notification && self.Notification.permission === 'granted')) {
    console.error('missing notification permission in service worker')
    return
  }

  const data = event.data?.json() ?? {}
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
