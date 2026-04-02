<template>
  <div class="space-y-6 max-w-2xl">

    <div>
      <h1 class="text-2xl font-bold text-slate-900">Configurações</h1>
      <p class="text-slate-500 mt-1">Personalize sua experiência no MentorIA</p>
    </div>

    <!-- Loading state -->
    <div v-if="loadingSettings" class="space-y-4">
      <UCard v-for="i in 3" :key="i">
        <div class="animate-pulse space-y-3">
          <div class="h-4 bg-slate-200 rounded w-1/3" />
          <div class="h-10 bg-slate-100 rounded" />
        </div>
      </UCard>
    </div>

    <template v-else>
      <!-- Notificações -->
      <UCard>
        <template #header>
          <h2 class="font-semibold text-slate-900">Notificações</h2>
        </template>
        <div class="space-y-4">
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
            <UInput v-model="settings.dailyGoalMinutes" type="number" min="15" max="720" />
          </UFormField>
          <UFormField label="Estilo de resposta do mentor">
            <USelect
              v-model="settings.mentorStyle"
              :options="mentorStyles"
              class="w-full"
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

      <UButton label="Salvar configurações" :loading="saving" @click="saveSettings" />
    </template>

  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: 'default', middleware: 'auth' })

const authStore = useAuthStore()
const { get, put } = useApi()
const toast = useToast()

const saving = ref(false)
const loadingSettings = ref(true)

const notifications = reactive([
  { key: 'dailyReminder', label: 'Lembrete diário', description: 'Receba um lembrete para estudar todo dia', enabled: true },
  { key: 'streakAlert', label: 'Alerta de sequência', description: 'Aviso quando sua sequência estiver em risco', enabled: true },
  { key: 'planUpdate', label: 'Atualizações do plano', description: 'Notificações sobre seu plano de estudos', enabled: false },
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

type SettingsResponse = {
  language: string
  dailyGoalMinutes: number
  mentorStyle: string
  dailyReminder: boolean
  streakAlert: boolean
  planUpdate: boolean
}

const loadSettings = async () => {
  loadingSettings.value = true
  try {
    const data = await get<SettingsResponse>('/users/me/settings')
    settings.language = data.language
    settings.dailyGoalMinutes = data.dailyGoalMinutes
    settings.mentorStyle = data.mentorStyle
    // Sync notification toggles
    const notifMap: Record<string, boolean> = {
      dailyReminder: data.dailyReminder,
      streakAlert: data.streakAlert,
      planUpdate: data.planUpdate,
    }
    for (const item of notifications) {
      if (item.key in notifMap) {
        item.enabled = notifMap[item.key]
      }
    }
  } catch (error) {
    console.error('Error loading settings:', error)
  } finally {
    loadingSettings.value = false
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    await put('/users/me/settings', {
      language: settings.language,
      dailyGoalMinutes: Number(settings.dailyGoalMinutes),
      mentorStyle: settings.mentorStyle,
      dailyReminder: notifications.find(n => n.key === 'dailyReminder')?.enabled ?? true,
      streakAlert: notifications.find(n => n.key === 'streakAlert')?.enabled ?? true,
      planUpdate: notifications.find(n => n.key === 'planUpdate')?.enabled ?? false,
    })
    toast.add({ title: 'Configurações salvas!', color: 'success' })
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao salvar configurações'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>