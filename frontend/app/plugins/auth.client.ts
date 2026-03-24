/**
 * Restaura Pinia a partir do localStorage e alinha com a sessão do Supabase (cookies),
 * chamando o backend para preencher `user` quando só o cliente Supabase tem sessão.
 */
import { useAuthStore } from '~/stores/auth'

export default defineNuxtPlugin(async () => {
  const authStore = useAuthStore()
  authStore.loadFromStorage()

  if (!import.meta.client) {
    return
  }

  const supabase = useSupabaseClient()
  const { data: { session } } = await supabase.auth.getSession()
  if (!session) {
    return
  }

  const needsSync = !authStore.accessToken || authStore.accessToken !== session.access_token
  if (!needsSync) {
    return
  }

  try {
    const config = useRuntimeConfig()
    const res = await fetch(`${config.public.apiBase}/auth/supabase/session`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        accessToken: session.access_token,
        refreshToken: session.refresh_token,
      }),
    })
    const json = await res.json()
    if (res.ok && json.success && json.data) {
      authStore.setAuth(json.data)
    }
  } catch {
    // rede / backend offline — mantém o que veio do storage
  }
})
