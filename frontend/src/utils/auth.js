export function saveTokens({ accessToken, refreshToken }) {
  localStorage.setItem('jwt', accessToken);
  document.cookie = `rt=${refreshToken}; Secure; SameSite=Strict; path=/`;
}

export function clearTokens() {
  localStorage.removeItem('jwt');
  document.cookie = 'rt=; Max-Age=0; path=/';
}

export async function apiFetch(url, options = {}, retry = true) {
  const token = localStorage.getItem('jwt');

  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
      ...(token && { Authorization: `Bearer ${token}` }),
    },
  });

  if ((res.status === 401 || res.status === 403) && retry) {
    const rt = document.cookie.split('; ').find(c => c.startsWith('rt='))?.split('=')[1];
    if (!rt) return forceLogout();

    const r = await fetch('/auth/refresh', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: rt }),
    });
    if (!r.ok) return forceLogout();

    const { accessToken } = await r.json();
    localStorage.setItem('jwt', accessToken);
    return apiFetch(url, options, false);
  }

  return res;
  function forceLogout() {
    clearTokens();
    window.location.href = '/login';
    return new Response(null, { status: 401, statusText: 'Logged out' });
  }
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch {
    return null;
  }
}