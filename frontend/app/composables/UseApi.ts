import { useAuthStore } from '~/stores/auth'

export const useApi = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()
  const router = useRouter()

  const request = async <T>(
    endpoint: string,
    options: RequestInit = {}
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

    // Token expirado — redireciona para login
    if (response.status === 401) {
      authStore.logout()
      router.push('/auth/login')
      throw new Error('Sessão expirada')
    }

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.error || 'Erro na requisição')
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