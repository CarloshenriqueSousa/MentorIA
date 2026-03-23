<template>
  <div class="space-y-6">

    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">
          Olá, {{ authStore.user?.name?.split(' ')[0] }}! 👋
        </h1>
        <p class="text-slate-500 mt-1">{{ greeting }}</p>
      </div>
      <NuxtLink to="/chat">
        <UButton label="Falar com Mentor" icon="i-heroicons-chat-bubble-left-right" />
      </NuxtLink>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <UCard v-for="stat in stats" :key="stat.label">
        <div class="flex items-center gap-3">
          <div :class="`w-10 h-10 ${stat.bg} rounded-lg flex items-center justify-center`">
            <UIcon :name="stat.icon" :class="`${stat.color} w-5 h-5`" />
          </div>
          <div>
            <p class="text-2xl font-bold text-slate-900">{{ stat.value }}</p>
            <p class="text-xs text-slate-500">{{ stat.label }}</p>
          </div>
        </div>
      </UCard>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

      <!-- Progresso semanal -->
      <div class="lg:col-span-2">
        <UCard>
          <template #header>
            <div class="flex items-center justify-between">
              <h2 class="font-semibold text-slate-900">Progresso da semana</h2>
              <UBadge :label="`${dashboard?.currentStreakDays || 0} dias seguidos 🔥`" color="warning" variant="subtle" />
            </div>
          </template>

          <div class="flex items-end gap-2 h-32">
            <div
              v-for="day in dashboard?.weeklyProgress || []"
              :key="day.date"
              class="flex-1 flex flex-col items-center gap-1"
            >
              <div
                class="w-full rounded-t-md transition-all"
                :class="day.minutesStudied > 0 ? 'bg-primary-500' : 'bg-slate-100'"
                :style="`height: ${Math.max(8, (day.minutesStudied / maxMinutes) * 100)}px`"
              />
              <span class="text-xs text-slate-400">{{ formatDay(day.date) }}</span>
            </div>
          </div>

          <template #footer>
            <div class="flex items-center justify-between text-sm text-slate-500">
              <span>Total: <strong class="text-slate-900">{{ totalHours }}h estudadas</strong></span>
              <UButton
                size="xs"
                variant="ghost"
                label="Registrar sessão"
                icon="i-heroicons-plus"
                @click="showProgressModal = true"
              />
            </div>
          </template>
        </UCard>
      </div>

      <!-- Plano ativo -->
      <UCard>
        <template #header>
          <h2 class="font-semibold text-slate-900">Plano de estudos</h2>
        </template>

        <div v-if="dashboard?.activePlan" class="space-y-4">
          <p class="text-sm font-medium text-slate-700">{{ dashboard.activePlan.title }}</p>
          <div>
            <div class="flex justify-between text-xs text-slate-500 mb-1">
              <span>Progresso</span>
              <span>{{ dashboard.activePlan.completionPercentage }}%</span>
            </div>
            <UProgress :value="dashboard.activePlan.completionPercentage" color="primary" />
          </div>
          <p class="text-xs text-slate-400">
            {{ dashboard.activePlan.daysRemaining }} dias restantes
          </p>
          <NuxtLink to="/study-plan">
            <UButton block variant="outline" size="sm" label="Ver plano completo" />
          </NuxtLink>
        </div>

        <div v-else class="text-center py-4">
          <UIcon name="i-heroicons-book-open" class="w-10 h-10 text-slate-300 mx-auto mb-3" />
          <p class="text-sm text-slate-500 mb-3">Nenhum plano ativo</p>
          <NuxtLink to="/study-plan">
            <UButton size="sm" label="Gerar plano com IA" />
          </NuxtLink>
        </div>
      </UCard>

    </div>

    <!-- Conquistas -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Conquistas</h2>
      </template>
      <div class="grid grid-cols-3 md:grid-cols-6 gap-4">
        <div
          v-for="achievement in dashboard?.achievements || []"
          :key="achievement.id"
          class="text-center"
        >
          <div
            class="w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-2 text-2xl"
            :class="achievement.unlocked ? 'bg-primary-100' : 'bg-slate-100 grayscale opacity-40'"
          >
            {{ achievement.icon }}
          </div>
          <p class="text-xs font-medium text-slate-700">{{ achievement.name }}</p>
        </div>
      </div>
    </UCard>

    <!-- Recomendação -->
    <UCard v-if="dashboard?.nextSessionRecommendation" class="bg-primary-50 border-primary-200">
      <div class="flex items-start gap-3">
        <UIcon name="i-heroicons-light-bulb" class="text-primary-600 w-5 h-5 mt-0.5 flex-shrink-0" />
        <div>
          <p class="text-sm font-medium text-primary-900">Recomendação do mentor</p>
          <p class="text-sm text-primary-700 mt-1">{{ dashboard.nextSessionRecommendation }}</p>
        </div>
        <NuxtLink to="/chat" class="ml-auto">
          <UButton size="xs" label="Estudar agora" />
        </NuxtLink>
      </div>
    </UCard>

    <!-- Modal registrar progresso -->
    <UModal v-model:open="showProgressModal">
      <template #content>
        <UCard>
          <template #header>
            <h3 class="font-semibold text-slate-900">Registrar sessão de estudos</h3>
          </template>
          <div class="space-y-4">
            <UFormField label="Minutos estudados">
              <UInput v-model="progressForm.minutesStudied" type="number" min="1" max="1440" />
            </UFormField>
            <UFormField label="Como foi seu humor? (1-5)">
              <div class="flex gap-2">
                <span
                  v-for="i in 5"
                  :key="i"
                  class="text-2xl cursor-pointer transition-transform hover:scale-125"
                  :class="progressForm.moodRating === i ? 'scale-125' : 'opacity-50'"
                  @click="progressForm.moodRating = i"
                >
                  {{ ['😞', '😕', '😐', '😊', '🤩'][i - 1] }}
                </span>
              </div>
            </UFormField>
            <UFormField label="Observações (opcional)">
              <UTextarea v-model="progressForm.notes" placeholder="Como foi a sessão?" />
            </UFormField>
          </div>
          <template #footer>
            <div class="flex justify-end gap-2">
              <UButton variant="ghost" label="Cancelar" @click="showProgressModal = false" />
              <UButton label="Salvar" :loading="savingProgress" @click="saveProgress" />
            </div>
          </template>
        </UCard>
      </template>
    </UModal>

  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({
  layout: 'default',
  middleware: 'auth',
})

const authStore = useAuthStore()
const { get, post } = useApi()
const toast = useToast()

type Achievement = {
  id: string
  icon: string
  name: string
  unlocked: boolean
}

type WeeklyProgressDay = {
  date: string
  minutesStudied: number
}

type ActivePlan = {
  title: string
  completionPercentage: number
  daysRemaining: number
}

type DashboardResponse = {
  totalXp: number
  currentStreakDays: number
  totalMinutesStudied: number
  totalExercisesDone: number
  achievements: Achievement[]
  weeklyProgress: WeeklyProgressDay[]
  activePlan?: ActivePlan | null
  nextSessionRecommendation?: string | null
}

const dashboard = ref<DashboardResponse | null>(null)
const showProgressModal = ref(false)
const savingProgress = ref(false)

const progressForm = reactive({
  minutesStudied: 60,
  moodRating: 3,
  notes: '',
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Bom dia! Pronto para estudar?'
  if (hour < 18) return 'Boa tarde! Vamos continuar?'
  return 'Boa noite! Uma última sessão?'
})

const stats = computed(() => [
  {
    label: 'Horas totais',
    value: `${Math.floor((dashboard.value?.totalMinutesStudied || 0) / 60)}h`,
    icon: 'i-heroicons-clock',
    bg: 'bg-blue-100',
    color: 'text-blue-600',
  },
  {
    label: 'Sequência atual',
    value: `${dashboard.value?.currentStreakDays || 0}🔥`,
    icon: 'i-heroicons-fire',
    bg: 'bg-orange-100',
    color: 'text-orange-600',
  },
  {
    label: 'XP Total',
    value: dashboard.value?.totalXp || 0,
    icon: 'i-heroicons-star',
    bg: 'bg-yellow-100',
    color: 'text-yellow-600',
  },
  {
    label: 'Exercícios',
    value: dashboard.value?.totalExercisesDone || 0,
    icon: 'i-heroicons-pencil-square',
    bg: 'bg-green-100',
    color: 'text-green-600',
  },
])

const maxMinutes = computed(() => {
  const progress = dashboard.value?.weeklyProgress ?? []
  return Math.max(...progress.map((d) => d.minutesStudied), 60)
})

const totalHours = computed(() => {
  const progress = dashboard.value?.weeklyProgress ?? []
  const totalMinutes = progress.reduce((acc, d) => acc + d.minutesStudied, 0)
  return (totalMinutes / 60).toFixed(1)
})

const formatDay = (date: string) => {
  const days = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S']
  return days[new Date(date).getDay()]
}

const saveProgress = async () => {
  savingProgress.value = true
  try {
    await post('/dashboard/progress', progressForm)
    toast.add({ title: 'Progresso salvo! 🎉', color: 'success' })
    showProgressModal.value = false
    await loadDashboard()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao salvar progresso'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    savingProgress.value = false
  }
}

const loadDashboard = async () => {
  try {
    dashboard.value = await get<DashboardResponse>('/dashboard')
  } catch (error) {
    console.error('Error loading dashboard:', error)
  }
}

onMounted(loadDashboard)
</script>