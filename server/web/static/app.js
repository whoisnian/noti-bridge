// ========================= indexedDB =========================
let DB = null
const TASKS = 'tasks'
const GROUPS = 'groups'
const KV = 'kv'
const dbOpen = async () => {
  if (DB != null) return DB
  return new Promise((resolve, reject) => {
    const openDBRequest = self.indexedDB.open('noti.db', 1)
    openDBRequest.onerror = reject
    openDBRequest.onsuccess = e => resolve(DB = e.target.result)
    openDBRequest.onupgradeneeded = e => {
      e.target.result.createObjectStore(TASKS, { keyPath: '_id', autoIncrement: true })
      e.target.result.createObjectStore(GROUPS, { keyPath: '_id', autoIncrement: true })
      e.target.result.createObjectStore(KV)
    }
  })
}

const dbAdd = async (name, value) => {
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

const dbGet = async (name, key) => {
  const db = await dbOpen()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction(name, 'readwrite')
      .objectStore(name)
      .get(key)
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

const dbPut = async (name, value, key) => {
  const db = await dbOpen()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction(name, 'readwrite')
      .objectStore(name)
      .put(value, key)
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

const dbDelete = async (name, key) => {
  const db = await dbOpen()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction(name, 'readwrite')
      .objectStore(name)
      .delete(key)
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

const dbGetAll = async (name) => {
  const db = await dbOpen()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction(name, 'readwrite')
      .objectStore(name)
      .getAll()
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

const dbDeleteAll = async (name) => {
  const db = await dbOpen()
  return new Promise((resolve, reject) => {
    const request = db
      .transaction(name, 'readwrite')
      .objectStore(name)
      .clear()
    request.onerror = reject
    request.onsuccess = e => resolve(e.target.result)
  })
}

// ========================= utility =========================
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

// ========================= element =========================
const createElement = (tag, options = {}) => {
  const element = document.createElement(tag)
  Object.entries(options).forEach(([k, v]) => {
    if (k === 'text') element.textContent = v
    else if (k === 'onclick') element.onclick = v
    else element.setAttribute(k, v)
  })
  return element
}

const copyText = (text) => {
  if (window.navigator && window.isSecureContext) {
    window.navigator.clipboard.writeText(text)
  } else {
    const input = createElement('input', { type: 'text', style: 'position:fixed;', value: text })
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    input.remove()
  }
}

const appendTaskLi = (parent, { Type, Title, Text, Link, CTime, _id }) => {
  const li = createElement('li')
  const br = createElement('br')

  li.appendChild(createElement('span', { style: 'font-weight:bold;', text: Type.toUpperCase() }))
  li.appendChild(createElement('span', { text: ` on ${dateStr(new Date(CTime))} ` }))

  if (Type !== 'ping') {
    li.appendChild(createElement('button', {
      text: 'copy',
      onclick: (e) => copyText(Type === 'link' ? Link : Text)
    }))
    li.appendChild(document.createTextNode(' '))
  }

  li.appendChild(createElement('button', {
    text: 'delete', onclick: async (e) => {
      parent.removeChild(li)
      parent.removeChild(br)
      await dbDelete(TASKS, _id)
    }
  }))

  if (['link', 'text'].includes(Type) && Title.length > 0) {
    li.appendChild(createElement('div', { text: htmlEscape(Title) }))
  }
  if (Type === 'link') {
    const linkDiv = createElement('div')
    linkDiv.appendChild(createElement('a', { href: Link, text: htmlEscape(Link) }))
    li.appendChild(linkDiv)
  } else if (Type === 'text') {
    li.appendChild(createElement('div', { text: htmlEscape(Text) }))
  }

  parent.appendChild(li)
  parent.appendChild(br)
  return li
}

const appendGroupTr = (parent, { gid, _id }) => {
  const tr = createElement('tr')

  tr.appendChild(createElement('td', { text: gid }))

  const td = createElement('td')
  td.appendChild(createElement('button', {
    text: 'delete', onclick: async (e) => {
      parent.removeChild(tr)
      await dbDelete(GROUPS, _id)
    }
  }))
  tr.appendChild(td)

  parent.appendChild(tr)
  return tr
}

// ========================= server api =========================
const updateDevice = async (sub) => {
  await dbPut(KV, sub.endpoint, 'token')
  await dbPut(KV, { Auth: arrayBufferToB64(sub.getKey('auth')), P256dh: arrayBufferToB64(sub.getKey('p256dh')) }, 'extra')

  const resp = await window.fetch('/api/device', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      Type: 1,
      Token: await dbGet(KV, 'token'),
      Name: await dbGet(KV, 'name'),
      Extra: await dbGet(KV, 'extra')
    })
  })
  if (resp.status != 200) window.alert('post /api/device error: ', resp.statusText)
}

const deleteDevice = async () => {
  const resp = await window.fetch('/api/device', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ Type: 1, Token: await dbGet(KV, 'token') })
  })
  if (resp.status != 200) window.alert('delete /api/device error: ', resp.statusText)
}

const bindGroups = async (ids) => {
  const resp = await window.fetch('/api/group', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      GIDs: ids,
      Type: 1,
      Token: await dbGet(KV, 'token'),
      Name: await dbGet(KV, 'name'),
      Extra: await dbGet(KV, 'extra')
    })
  })
  if (resp.status != 200) window.alert('put /api/group error: ', resp.statusText)
}

const unbindGroups = async (ids) => {
  const resp = await window.fetch('/api/group', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ GIDs: ids, Type: 1, Token: await dbGet(KV, 'token') })
  })
  if (resp.status != 200) window.alert('delete /api/group error: ', resp.statusText)
}

// ========================= main =========================
(async () => {
  if (!('serviceWorker' in navigator)) {
    return window.alert('This browser does not support service worker.')
  } else if (!('Notification' in window)) {
    return window.alert('This browser does not support notification.')
  }

  const resp = await fetch('/server-key')
  if (resp.status != 200) return window.alert('get /server-key error: ', resp.statusText)
  const applicationServerKey = await resp.text()

  const name = await dbGet(KV, 'name')
  if (!name) {
    await dbPut(KV, (/Firefox\/\d+/i).exec(navigator.userAgent)?.[0] ?? (/Chrome\/\d+/i).exec(navigator.userAgent)?.[0] ?? 'Other', 'name')
  }

  const statusSpan = document.getElementById('status_span')
  const statusBtn = document.getElementById('status_btn')
  const reloadBtn = document.getElementById('reload_btn')
  const clearBtn = document.getElementById('clear_btn')
  const manageBtn = document.getElementById('manage_btn')
  const tasksUl = document.getElementById('tasks_ul')

  const manageDialog = document.getElementById('manage_dialog')
  const manageTbody = document.getElementById('manage_tbody')
  const nameText = document.getElementById('name_text')
  const nameBtn = document.getElementById('name_btn')
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
      const groups = await dbGetAll(GROUPS)
      await unbindGroups(groups.map(g => g.gid))
      await deleteDevice()
      await sub.unsubscribe()
      window.location.reload()
    }
    await updateDevice(sub)
  }

  reloadBtn.onclick = (e) => window.location.reload()
  clearBtn.onclick = async (e) => {
    while (tasksUl.firstChild) tasksUl.removeChild(tasksUl.firstChild)
    await dbDeleteAll(TASKS)
  }
  manageBtn.onclick = async (e) => {
    while (manageTbody.firstChild) manageTbody.removeChild(manageTbody.firstChild)
    const groups = await dbGetAll(GROUPS)
    groups.forEach(group => appendGroupTr(manageTbody, group))
    manageDialog.showModal()
  }

  const tasks = await dbGetAll(TASKS)
  tasks.forEach(task => appendTaskLi(tasksUl, task))

  manageDialog.onclick = async (e) => {
    if (e.target !== manageDialog) return
    const { left, top, height, width } = manageDialog.getBoundingClientRect()
    if (e.clientY < top || e.clientY > top + height || e.clientX < left || e.clientX > left + width)
      manageDialog.close()
  }

  nameText.value = await dbGet(KV, 'name')
  nameBtn.onclick = async (e) => {
    e.preventDefault()
    await dbPut(KV, nameText.value, 'name')
    manageDialog.close()
  }

  joinBtn.onclick = async (e) => {
    e.preventDefault()
    await bindGroups([joinText.value])
    await dbAdd(GROUPS, { gid: joinText.value })
    manageDialog.close()
  }
})()
