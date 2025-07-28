// src/utils/auth.js
export function saveTokens({ accessToken, refreshToken }) {
  localStorage.setItem('jwt', accessToken);
  document.cookie = `rt=${refreshToken}; Secure; SameSite=Strict; path=/`;
}

export function clearTokens() {
  localStorage.removeItem('jwt');
  document.cookie = 'rt=; Max-Age=0; path=/';
}

/* universal fetch wrapper with auto-refresh */
export async function apiFetch(url, options = {}, retry = true) {   
      console.log('[apiFetch]', url, 'retry', retry);
  const token = localStorage.getItem('jwt');
  const res = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}), ...(token && { Authorization: `Bearer ${token}` }) }
  });

  if ((res.status === 401 || res.status === 403) && retry) {
    // try refresh once
    const rt = document.cookie.split('; ').find(c => c.startsWith('rt='))?.split('=')[1];
    if (!rt) return forceLogout();

    const r = await fetch('/auth/refresh', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: rt })
    });
    if (!r.ok) return forceLogout();

    const { accessToken } = await r.json();
    localStorage.setItem('jwt', accessToken);
    return apiFetch(url, options, false); // retry original call
  }
  return res;

  function forceLogout() {
    clearTokens();
    window.location.href = '/login';
  }
}
