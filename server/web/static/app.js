const arrayBufferToB64 = (buffer) => {
  return window.btoa(
    new Uint8Array(buffer).reduce((res, b) => {
      return res + String.fromCharCode(b)
    }, '')
  )
}

let DB = null
const openDB = async () => {
  if (DB != null) return DB
  return new Promise((resolve, reject) => {
    const openDBRequest = window.indexedDB.open('noti.db', 1)
    openDBRequest.onerror = reject
    openDBRequest.onsuccess = e => resolve(DB = e.target.result)
    openDBRequest.onupgradeneeded = e => e.target.result.createObjectStore('tasks', { autoIncrement: true })
  })
}

const getAllTasks = async () => {
  const db = await openDB()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction('tasks', 'readwrite')
      .objectStore('tasks')
      .getAll()
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

(async () => {
  if (!('serviceWorker' in navigator)) {
    return window.alert('This browser does not support service worker.')
  } else if (!('Notification' in window)) {
    return window.alert('This browser does not support notification.')
  } else if (Notification.permission !== 'granted') {
    const perm = await Notification.requestPermission()
    if (perm !== 'granted') return window.alert('Missing notification permission.')
  }

  const tasks = await getAllTasks()
  console.log(tasks)

  const resp1 = await fetch('/server-key')
  if (resp1.status != 200) return window.alert('get /server-key error: ', resp1.statusText)
  const applicationServerKey = await resp1.text()

  const reg = await navigator.serviceWorker.register('/static/sw.js')
  let sub = await reg.pushManager.getSubscription()
  if (sub === null) {
    sub = await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey })
  }

  const regexF = /Firefox\/\d+/i
  const regexC = /Chrome\/\d+/i
  const Name = regexF.exec(navigator.userAgent)?.[0] ?? regexC.exec(navigator.userAgent)?.[0] ?? 'Other'
  const resp2 = await window.fetch('/api/device', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      Type: 1,
      Token: sub.endpoint,
      Name,
      Extra: {
        Auth: arrayBufferToB64(sub.getKey('auth')),
        P256dh: arrayBufferToB64(sub.getKey('p256dh'))
      }
    })
  })
  if (resp2.status != 200) return window.alert('post /api/device error: ', resp2.statusText)
})()
