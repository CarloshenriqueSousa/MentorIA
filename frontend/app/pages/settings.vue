<template>
  <div class="space-y-6 max-w-2xl">

    <div>
      <h1 class="text-2xl font-bold text-slate-900">Configurações</h1>
      <p class="text-slate-500 mt-1">Personalize sua experiência no MentorIA</p>
    </div>

    <!-- Notificações -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Notificações</h2>
      </template>
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 3" :key="i" class="flex items-center justify-between">
          <div class="space-y-2">
            <USkeleton class="h-4 w-32" />
            <USkeleton class="h-3 w-48" />
          </div>
          <USkeleton class="h-6 w-10 rounded-full" />
        </div>
      </div>
      <div v-else class="space-y-4">
        <div v-for="item in notifications" :key="item.key" class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-900">{{ item.label }}</p>
            <p class="text-xs text-slate-500">{{ item.description }}</p>
          </div>
          <UToggle v-model="item.enabled" />
        </div>
      </div>
    </UCard>

    <!-- Idioma -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Idioma e região</h2>
      </template>
      <div class="space-y-4">
        <UFormField label="Idioma">
          <USelect
            v-model="settings.language"
            :options="languages"
            class="w-full"
            :loading="loading"
          />
        </UFormField>
      </div>
    </UCard>

    <!-- Preferências de estudo -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Preferências de estudo</h2>
      </template>
      <div class="space-y-4">
        <UFormField label="Meta diária de estudo (minutos)">
          <UInput v-model="settings.dailyGoalMinutes" type="number" min="15" max="720" :disabled="loading" />
        </UFormField>
        <UFormField label="Estilo de resposta do mentor">
          <USelect
            v-model="settings.mentorStyle"
            :options="mentorStyles"
            class="w-full"
            :loading="loading"
          />
        </UFormField>
      </div>
    </UCard>

    <!-- Assinatura -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Assinatura</h2>
      </template>
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-slate-900">Plano {{ authStore.user?.planType }}</p>
          <p class="text-xs text-slate-500">
            {{ authStore.user?.planType === 'FREE' ? 'Plano gratuito' : 'Renovação automática mensal' }}
          </p>
        </div>
        <NuxtLink to="/pricing">
          <UButton
            :label="authStore.user?.planType === 'FREE' ? 'Fazer upgrade' : 'Gerenciar plano'"
            :variant="authStore.user?.planType === 'FREE' ? 'solid' : 'outline'"
            size="sm"
          />
        </NuxtLink>
      </div>
    </UCard>

    <UButton label="Salvar configurações" :loading="saving" :disabled="loading" @click="saveSettings" />

  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: 'default', middleware: 'auth' })

const authStore = useAuthStore()
const { get, put } = useApi()
const toast = useToast()
const saving = ref(false)
const loading = ref(true)

const notifications = reactive([
  { key: 'daily_reminder', label: 'Lembrete diário', description: 'Receba um lembrete para estudar todo dia', enabled: true },
  { key: 'streak_alert', label: 'Alerta de sequência', description: 'Aviso quando sua sequência estiver em risco', enabled: true },
  { key: 'plan_update', label: 'Atualizações do plano', description: 'Notificações sobre seu plano de estudos', enabled: false },
])

const settings = reactive({
  language: 'pt-BR',
  dailyGoalMinutes: 120,
  mentorStyle: 'BALANCED',
})

const languages = [
  { label: 'Português (BR)', value: 'pt-BR' },
  { label: 'English', value: 'en' },
]

const mentorStyles = [
  { label: 'Equilibrado', value: 'BALANCED' },
  { label: 'Detalhado e explicativo', value: 'DETAILED' },
  { label: 'Direto e objetivo', value: 'DIRECT' },
  { label: 'Encorajador e motivador', value: 'ENCOURAGING' },
]

const loadSettings = async () => {
  loading.value = true
  try {
    const profile = await get<any>('/onboarding')
    if (profile) {
      settings.dailyGoalMinutes = profile.dailyGoalMinutes || 120
      settings.mentorStyle = profile.mentorStyle || 'BALANCED'
      
      const extra = profile.extraInfo || {}
      if (extra.language) settings.language = extra.language
      if (extra.notifications) {
        notifications.forEach(n => {
          if (extra.notifications[n.key] !== undefined) {
            n.enabled = extra.notifications[n.key]
          }
        })
      }
    }
  } catch (error) {
    console.error('Error loading settings:', error)
  } finally {
    loading.value = false
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    const notificationMap = notifications.reduce((acc, n) => {
      acc[n.key] = n.enabled
      return acc
    }, {} as Record<string, boolean>)

    await put('/users/settings', {
      language: settings.language,
      dailyGoalMinutes: settings.dailyGoalMinutes,
      mentorStyle: settings.mentorStyle,
      notifications: notificationMap
    })

    toast.add({ title: 'Configurações salvas!', color: 'success' })
  } catch (error: any) {
    toast.add({ 
      title: 'Erro ao salvar configurações', 
      description: error.message || 'Erro desconhecido', 
      color: 'error' 
    })
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>