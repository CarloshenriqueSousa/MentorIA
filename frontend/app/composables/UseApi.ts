import { useAuthStore, type User } from '~/stores/auth'

type ApiEnvelope<T> = {
  success?: boolean
  data?: T
  error?: string
  message?: string
}

function normalizeUser(user: User): User {
  return {
    ...user,
    id: String(user.id),
  }
}

export const useApi = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()
  const router = useRouter()

  const apiBase = String(config.public.apiBase || '/api').replace(/\/$/, '')

  const tryRefreshSession = async (): Promise<boolean> => {
    if (!import.meta.client || !authStore.refreshToken?.trim()) {
      return false
    }

    try {
      const res = await fetch(`${apiBase}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Refresh-Token': authStore.refreshToken.trim(),
        },
      })
      const json = await res.json() as ApiEnvelope<{ accessToken: string; refreshToken: string; user: User }>
      if (!res.ok || !json?.success || !json?.data?.accessToken) {
        return false
      }
      authStore.setAuth({
        ...json.data,
        user: normalizeUser(json.data.user),
      })
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
    const path = endpoint.startsWith('/') ? endpoint : `/${endpoint}`
    const url = `${apiBase}${path}`

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...(options.headers as Record<string, string>),
    }

    if (authStore.accessToken) {
      headers.Authorization = `Bearer ${authStore.accessToken}`
    }

    let response: Response
    try {
      response = await fetch(url, {
        ...options,
        headers,
      })
    } catch {
      throw new Error(
        'Não foi possível conectar à API. Verifique se o backend está rodando (porta 8080) e se o PostgreSQL está ativo.',
      )
    }

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
    let data: ApiEnvelope<T>
    try {
      data = text ? (JSON.parse(text) as ApiEnvelope<T>) : {}
    } catch {
      if (!response.ok) {
        throw new Error(response.statusText || `Erro HTTP ${response.status}`)
      }
      throw new Error('Resposta inválida do servidor')
    }

    if (!response.ok || data.success === false) {
      const validation = data.data as Record<string, string> | undefined
      const validationMsg = validation && typeof validation === 'object'
        ? Object.values(validation)[0]
        : undefined
      throw new Error(data.error || validationMsg || data.message || `Erro HTTP ${response.status}`)
    }

    if (data.data === undefined || data.data === null) {
      throw new Error('Resposta vazia do servidor')
    }

    return data.data
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
