import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware((to) => {
  const authStore = useAuthStore()
  authStore.loadFromStorage()

  const publicRoutes = ['/', '/auth/login', '/auth/register', '/terms', '/privacy']
  const isPublic = publicRoutes.includes(to.path)

  // Não logado tentando acessar rota privada
  if (!authStore.isLoggedIn && !isPublic) {
    return navigateTo('/auth/login')
  }

  // Logado mas sem onboarding (admins podem acessar /admin para operar o painel)
  if (
    authStore.isLoggedIn
    && authStore.needsOnboarding
    && to.path !== '/onboarding'
    && !(authStore.user?.role === 'ADMIN' && to.path.startsWith('/admin'))
  ) {
    return navigateTo('/onboarding')
  }

  // Já logado tentando acessar login/registro
  if (authStore.isLoggedIn && (to.path === '/auth/login' || to.path === '/auth/register')) {
    return navigateTo('/dashboard')
  }
})