const arrayBufferToB64 = (buffer) => {
  return window.btoa(
    new Uint8Array(buffer).reduce((res, b) => {
      return res + String.fromCharCode(b)
    }, '')
  )
}

const ESCAPE = {
  '<': '&lt;',
  '>': '&gt;',
  '&': '&amp;',
  '"': '&#34;',
  '\'': '&#39;',
  ' ': '&nbsp;',
  '\n': '<br />'
}
const htmlEscape = str => str.replace(/[<>&"' \n]/g, ch => ESCAPE[ch])

const dateStr = (date) => `${date.getFullYear()}-` +
  `${('0' + (date.getMonth() + 1)).slice(-2)}-` +
  `${('0' + date.getDate()).slice(-2)} ` +
  `${('0' + date.getHours()).slice(-2)}:` +
  `${('0' + date.getMinutes()).slice(-2)}:` +
  `${('0' + date.getSeconds()).slice(-2)}`

const createElement = (tag, options = {}) => {
  const element = document.createElement(tag)
  Object.entries(options).forEach(([k, v]) => {
    element.setAttribute(k, v)
  })
  return element
}

const createTaskLi = ({ Type, Title, Text, Link, CTime }) => {
  const li = createElement('li')

  const typeSpan = createElement('span', { style: 'font-weight:bold;' })
  typeSpan.textContent = Type.toUpperCase()
  li.appendChild(typeSpan)

  const dateSpan = createElement('span')
  dateSpan.textContent = ` on ${dateStr(new Date(CTime))} `
  li.appendChild(dateSpan)

  if (Type !== 'ping') {
    const copyBtn = createElement('button')
    copyBtn.textContent = 'copy'
    li.appendChild(copyBtn)
    li.appendChild(document.createTextNode(' '))
  }

  const deleteBtn = createElement('button')
  deleteBtn.textContent = 'delete'
  li.appendChild(deleteBtn)

  if (Type === 'ping') return li

  if (Title.length > 0) {
    const titleDiv = createElement('div')
    titleDiv.textContent = htmlEscape(Title)
    li.appendChild(titleDiv)
  }
  if (Type === 'link') {
    const linkDiv = createElement('div')
    const aLink = createElement('a', { href: Link })
    aLink.textContent = htmlEscape(Link)
    linkDiv.appendChild(aLink)
    li.appendChild(linkDiv)
  } else if (Type === 'text') {
    const textDiv = createElement('div')
    textDiv.textContent = htmlEscape(Text)
    li.appendChild(textDiv)
  }
  return li
}

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

const deleteAllTasks = async () => {
  const db = await openDB()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction('tasks', 'readwrite')
      .objectStore('tasks')
      .clear()
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

const updateDevice = async (sub) => {
  const regexF = /Firefox\/\d+/i
  const regexC = /Chrome\/\d+/i
  const resp = await window.fetch('/api/device', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      Type: 1, Token: sub.endpoint,
      Name: regexF.exec(navigator.userAgent)?.[0] ?? regexC.exec(navigator.userAgent)?.[0] ?? 'Other',
      Extra: {
        Auth: arrayBufferToB64(sub.getKey('auth')),
        P256dh: arrayBufferToB64(sub.getKey('p256dh'))
      }
    })
  })
  if (resp.status != 200) window.alert('post /api/device error: ', resp.statusText)
}

const deleteDevice = async (sub) => {
  const resp = await window.fetch('/api/device', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ Type: 1, Token: sub.endpoint })
  })
  if (resp.status != 200) window.alert('delete /api/device error: ', resp.statusText)
}

(async () => {
  if (!('serviceWorker' in navigator)) {
    return window.alert('This browser does not support service worker.')
  } else if (!('Notification' in window)) {
    return window.alert('This browser does not support notification.')
  }

  const resp = await fetch('/server-key')
  if (resp.status != 200) return window.alert('get /server-key error: ', resp.statusText)
  const applicationServerKey = await resp.text()

  const statusSpan = document.getElementById('status_span')
  const statusBtn = document.getElementById('status_btn')
  const clearBtn = document.getElementById('clear_btn')
  const manageBtn = document.getElementById('manage_btn')
  const tasksUl = document.getElementById('tasks_ul')

  const manageDialog = document.getElementById('manage_dialog')
  const manageTable = document.getElementById('manage_table')
  const joinForm = document.getElementById('join_form')
  const joinText = document.getElementById('join_text')
  const joinBtn = document.getElementById('join_btn')

  const reg = await navigator.serviceWorker.register('/static/sw.js')
  const sub = await reg.pushManager.getSubscription()
  if (sub === null) {
    statusSpan.textContent = 'NULL'
    statusSpan.style.color = 'red'
    statusBtn.textContent = 'subscribe'
    statusBtn.onclick = async (e) => {
      if (Notification.permission !== 'granted') {
        const perm = await Notification.requestPermission()
        if (perm !== 'granted') return window.alert('Missing notification permission.')
      }
      const newSub = await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey })
      await updateDevice(newSub)
      window.location.reload()
    }
  } else {
    statusSpan.textContent = 'OK'
    statusSpan.style.color = 'green'
    statusBtn.textContent = 'unsubscribe'
    statusBtn.onclick = async (e) => {
      await deleteDevice(sub)
      await sub.unsubscribe()
    }
    await updateDevice(sub)
  }

  clearBtn.onclick = async (e) => {
    while (tasksUl.firstChild)
      tasksUl.removeChild(tasksUl.firstChild)
    await deleteAllTasks()
  }
  manageBtn.onclick = async (e) => {
    manageDialog.showModal()
  }

  const tasks = await getAllTasks()
  tasks.forEach(task => {
    tasksUl.appendChild(createTaskLi(task))
    tasksUl.appendChild(document.createElement('br'))
  })

  manageDialog.onclick = async (e) => {
    if (e.target !== manageDialog) return
    const { left, top, height, width } = manageDialog.getBoundingClientRect()
    if (e.clientY < top || e.clientY > top + height || e.clientX < left || e.clientX > left + width)
      manageDialog.close()
  }
})()
