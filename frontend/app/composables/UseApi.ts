import { useAuthStore } from '~/stores/auth'

export const useApi = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()
  const router = useRouter()

  const tryRefreshSession = async (): Promise<boolean> => {
    if (!import.meta.client || !authStore.refreshToken?.trim()) {
      return false
    }

    try {
      const res = await fetch(`${config.public.apiBase}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Refresh-Token': authStore.refreshToken.trim(),
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
      const refreshed = await tryRefreshSession()
      if (refreshed) {
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
