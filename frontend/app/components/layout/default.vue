<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-slate-200 transform transition-transform duration-300',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      ]"
    >
      <!-- Logo -->
      <div class="flex items-center gap-3 px-6 py-5 border-b border-slate-200">
        <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
          <UIcon name="i-heroicons-academic-cap" class="text-white w-5 h-5" />
        </div>
        <span class="font-bold text-slate-900 text-lg">MentorIA</span>
      </div>

      <!-- Navigation -->
      <nav class="p-4 space-y-1">
        <NuxtLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path === item.to
            ? 'bg-primary-50 text-primary-700'
            : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
        >
          <UIcon :name="item.icon" class="w-5 h-5 flex-shrink-0" />
          {{ item.label }}
        </NuxtLink>
      </nav>

      <!-- User info at bottom -->
      <div class="absolute bottom-0 left-0 right-0 p-4 border-t border-slate-200">
        <div class="flex items-center gap-3">
          <UAvatar
            :alt="authStore.user?.name"
            size="sm"
          />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-slate-900 truncate">{{ authStore.user?.name }}</p>
            <UBadge
              :label="authStore.user?.planType"
              :color="authStore.user?.planType === 'FREE' ? 'neutral' : 'primary'"
              size="xs"
              variant="subtle"
            />
          </div>
          <UButton
            icon="i-heroicons-arrow-right-on-rectangle"
            variant="ghost"
            color="neutral"
            size="xs"
            @click="logout"
          />
        </div>
      </div>
    </aside>

    <!-- Mobile overlay -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-40 bg-black/50 lg:hidden"
      @click="sidebarOpen = false"
    />

    <!-- Main content -->
    <div class="lg:pl-64">
      <!-- Top bar -->
      <header class="sticky top-0 z-30 bg-white border-b border-slate-200 px-4 py-3 flex items-center gap-4">
        <UButton
          icon="i-heroicons-bars-3"
          variant="ghost"
          color="neutral"
          class="lg:hidden"
          @click="sidebarOpen = !sidebarOpen"
        />
        <div class="flex-1" />
        <!-- Notificações -->
        <UButton
          icon="i-heroicons-bell"
          variant="ghost"
          color="neutral"
          size="sm"
        />
      </header>

      <!-- Page content -->
      <main class="p-6">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const sidebarOpen = ref(false)

const navItems = computed(() => {
  const items = [
    { to: '/dashboard', icon: 'i-heroicons-squares-2x2', label: 'Dashboard' },
    ...(authStore.user?.role === 'ADMIN'
      ? [{ to: '/admin', icon: 'i-heroicons-shield-check', label: 'Painel admin' } as const]
      : []),
    { to: '/chat', icon: 'i-heroicons-chat-bubble-left-right', label: 'Chat com Mentor' },
    { to: '/chat/history', icon: 'i-heroicons-clock', label: 'Histórico' },
    { to: '/study-plan', icon: 'i-heroicons-book-open', label: 'Plano de Estudos' },
    { to: '/profile', icon: 'i-heroicons-user', label: 'Perfil' },
    { to: '/settings', icon: 'i-heroicons-cog-6-tooth', label: 'Configurações' },
    { to: '/pricing', icon: 'i-heroicons-star', label: 'Upgrade' },
  ]
  return items
})

const supabase = useSupabaseClient()

const logout = async () => {
  await supabase.auth.signOut()
  authStore.logout()
  router.push('/auth/login')
}
</script>