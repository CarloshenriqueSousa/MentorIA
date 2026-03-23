/**
 * Plugin client-side que restaura o estado de autenticação do localStorage
 * ao carregar a aplicação, evitando logout ao recarregar a página.
 */
import { useAuthStore } from '~/stores/auth'

export default defineNuxtPlugin(() => {
  const authStore = useAuthStore()
  authStore.loadFromStorage()
})
