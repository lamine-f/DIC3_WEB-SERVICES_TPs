(() => {
  const messagesEl = document.getElementById('messages');
  const formEl = document.getElementById('send-form');
  const contentEl = document.getElementById('content');
  const roomId = Number(messagesEl.dataset.roomId);

  let sinceId = 0;
  let polling = false;

  function appendMessage(m) {
    const div = document.createElement('div');
    div.className = 'message';
    const time = m.createdAt ? new Date(m.createdAt).toLocaleTimeString() : '';
    div.innerHTML = `<span class="author">${escapeHtml(m.username)}</span>` +
      `<span class="time">${escapeHtml(time)}</span>` +
      `<div class="content">${escapeHtml(m.content)}</div>`;
    messagesEl.appendChild(div);
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  async function poll() {
    if (polling) return;
    polling = true;
    try {
      const r = await fetch(`api-proxy.php?action=getMessages&roomId=${roomId}&sinceId=${sinceId}`);
      if (!r.ok) return;
      const data = await r.json();
      const list = data.messages || [];
      for (const m of list) {
        appendMessage(m);
        if (m.id > sinceId) sinceId = m.id;
      }
    } catch (e) {
      console.error('poll error', e);
    } finally {
      polling = false;
    }
  }

  formEl.addEventListener('submit', async (e) => {
    e.preventDefault();
    const content = contentEl.value.trim();
    if (!content) return;
    contentEl.disabled = true;
    try {
      const body = new URLSearchParams({ roomId: String(roomId), content });
      const r = await fetch('api-proxy.php?action=sendMessage', {
        method: 'POST',
        body,
      });
      if (!r.ok) {
        const err = await r.json().catch(() => ({}));
        alert('Erreur envoi : ' + (err.error || r.status));
      } else {
        contentEl.value = '';
        poll();
      }
    } finally {
      contentEl.disabled = false;
      contentEl.focus();
    }
  });

  poll();
  setInterval(poll, 1500);
})();
