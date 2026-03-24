import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware(() => {
  const authStore = useAuthStore()
  authStore.loadFromStorage()

  if (!authStore.isLoggedIn) {
    return navigateTo('/auth/login')
  }
  if (authStore.user?.role !== 'ADMIN') {
    return navigateTo('/dashboard')
  }
})
