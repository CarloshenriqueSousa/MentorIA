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

  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: 'default', middleware: 'auth' })

const authStore = useAuthStore()
const toast = useToast()
const saving = ref(false)

const notifications = reactive([
  { key: 'daily_reminder', label: 'Lembrete diário', description: 'Receba um lembrete para estudar todo dia', enabled: true },
  { key: 'streak_alert', label: 'Alerta de sequência', description: 'Aviso quando sua sequência estiver em risco', enabled: true },
  { key: 'plan_update', label: 'Atualizações do plano', description: 'Notificações sobre seu plano de estudos', enabled: false },
])

const settings = reactive({
  language: 'pt-BR',
  dailyGoalMinutes: 120,
  mentorStyle: 'balanced',
})

const languages = [
  { label: 'Português (BR)', value: 'pt-BR' },
  { label: 'English', value: 'en' },
]

const mentorStyles = [
  { label: 'Equilibrado', value: 'balanced' },
  { label: 'Detalhado e explicativo', value: 'detailed' },
  { label: 'Direto e objetivo', value: 'direct' },
  { label: 'Encorajador e motivador', value: 'encouraging' },
]

const saveSettings = async () => {
  saving.value = true
  await new Promise(r => setTimeout(r, 800))
  toast.add({ title: 'Configurações salvas!', color: 'green' })
  saving.value = false
}
</script>