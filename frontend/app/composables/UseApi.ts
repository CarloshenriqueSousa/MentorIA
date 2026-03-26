import { useAuthStore } from '~/stores/auth'

async function tryRefreshSupabaseSession(authStore: ReturnType<typeof useAuthStore>): Promise<boolean> {
  if (!import.meta.client) {
    return false
  }
  try {
    const supabase = useSupabaseClient()
    const { data, error } = await supabase.auth.refreshSession()
    if (error || !data.session) {
      return false
    }
    authStore.patchTokens(data.session.access_token, data.session.refresh_token)
    return true
  } catch {
    return false
  }
}

async function tryRefreshLocalSession(
  authStore: ReturnType<typeof useAuthStore>,
): Promise<boolean> {
  if (!import.meta.client) {
    return false
  }
  if (!authStore.refreshToken) {
    return false
  }
  const refresh = authStore.refreshToken.trim()
  if (!refresh) {
    return false
  }

  try {
    const config = useRuntimeConfig()
    const res = await fetch(`${config.public.apiBase}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Refresh-Token': refresh,
      },
    })
    const json = await res.json()
    if (!res.ok || !json?.success || !json?.data?.accessToken) {
      return false
    }
    authStore.setAuth(json.data)
    return true
  } catch {
    return false
  }
}

export const useApi = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()
  const router = useRouter()

  const request = async <T>(
    endpoint: string,
    options: RequestInit = {},
    isRetry = false,
  ): Promise<T> => {
    const url = `${config.public.apiBase}${endpoint}`

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    }

    if (authStore.accessToken) {
      headers['Authorization'] = `Bearer ${authStore.accessToken}`
    }

    const response = await fetch(url, {
      ...options,
      headers,
    })

    if (response.status === 401 && !isRetry && import.meta.client) {
      // 1) tenta refresh via Supabase (quando token veio do Supabase)
      const refreshedSupabase = await tryRefreshSupabaseSession(authStore)
      if (refreshedSupabase) {
        return request<T>(endpoint, options, true)
      }

      // 2) fallback: refresh local do backend (quando login foi /auth/login)
      const refreshedLocal = await tryRefreshLocalSession(authStore)
      if (refreshedLocal) {
        return request<T>(endpoint, options, true)
      }

      authStore.logout()
      await router.push('/auth/login')
      throw new Error('Sessão expirada')
    }

    const text = await response.text()
    let data: { data?: T; error?: string; message?: string }
    try {
      data = text ? (JSON.parse(text) as typeof data) : {}
    } catch {
      if (!response.ok) {
        throw new Error(response.statusText || 'Erro na requisição')
      }
      throw new Error('Resposta inválida do servidor')
    }

    if (!response.ok) {
      throw new Error(data.error || data.message || 'Erro na requisição')
    }

    return data.data as T
  }

  return {
    get: <T>(endpoint: string) => request<T>(endpoint, { method: 'GET' }),

    post: <T>(endpoint: string, body: unknown) =>
      request<T>(endpoint, {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    put: <T>(endpoint: string, body: unknown) =>
      request<T>(endpoint, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),

    patch: <T>(endpoint: string, body?: unknown) =>
      request<T>(endpoint, {
        method: 'PATCH',
        body: body ? JSON.stringify(body) : undefined,
      }),

    del: <T>(endpoint: string) => request<T>(endpoint, { method: 'DELETE' }),
  }
}
