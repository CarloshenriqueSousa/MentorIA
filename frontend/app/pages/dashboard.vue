<template>
  <div class="space-y-8">

    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-3xl font-extrabold text-slate-900 dark:text-white tracking-tight font-outfit">
          Olá, {{ authStore.user?.name?.split(' ')[0] }}! 👋
        </h1>
        <p class="text-slate-500 dark:text-slate-400 mt-1 text-lg">{{ greeting }}</p>
      </div>
      <NuxtLink to="/chat">
        <UButton 
          label="Falar com Mentor" 
          icon="i-heroicons-chat-bubble-left-right" 
          size="lg"
          class="shadow-lg shadow-primary-500/20 active:scale-95 transition-transform"
        />
      </NuxtLink>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-6">
      <div 
        v-for="stat in stats" 
        :key="stat.label"
        class="group p-6 rounded-2xl bg-white dark:bg-slate-900 border border-stone-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-all duration-300"
      >
        <div class="flex items-center gap-4">
          <div :class="`w-12 h-12 ${stat.bg} dark:bg-opacity-10 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform`">
            <UIcon :name="stat.icon" :class="`${stat.color} w-6 h-6`" />
          </div>
          <div>
            <p class="text-2xl font-black text-slate-900 dark:text-white font-outfit">{{ stat.value }}</p>
            <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider">{{ stat.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

      <!-- Progresso semanal -->
      <div class="lg:col-span-2">
        <div class="rounded-2xl bg-white dark:bg-slate-900 border border-stone-200 dark:border-slate-800 shadow-sm p-6 overflow-hidden relative">
          <div class="flex items-center justify-between mb-8">
            <h2 class="font-bold text-slate-900 dark:text-white text-lg font-outfit">Progresso da semana</h2>
            <UBadge :label="`${dashboard?.currentStreakDays || 0} dias seguidos 🔥`" color="warning" variant="subtle" class="animate-pulse" />
          </div>

          <div class="flex items-end gap-3 h-48 relative z-10">
            <div
              v-for="day in dashboard?.weeklyProgress || []"
              :key="day.date"
              class="flex-1 flex flex-col items-center gap-2 group"
            >
              <div class="relative w-full flex flex-col items-center">
                <div 
                  class="absolute -top-8 bg-slate-800 text-white text-[10px] px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  {{ day.minutesStudied }}m
                </div>
                <div
                  class="w-full rounded-xl transition-all duration-500 ease-out cursor-pointer"
                  :class="day.minutesStudied > 0 ? 'bg-gradient-to-t from-primary-600 to-primary-400' : 'bg-slate-100 dark:bg-slate-800'"
                  :style="`height: ${Math.max(12, (day.minutesStudied / maxMinutes) * 100)}%`"
                />
              </div>
              <span class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase">{{ formatDay(day.date) }}</span>
            </div>
          </div>

          <div class="mt-8 pt-6 border-t border-stone-100 dark:border-slate-800 flex items-center justify-between">
            <div class="flex flex-col">
              <span class="text-xs text-slate-500 dark:text-slate-400">Tempo total</span>
              <span class="text-lg font-bold text-slate-900 dark:text-white font-outfit">{{ totalHours }}h estudadas</span>
            </div>
            <UButton
              variant="soft"
              label="Registrar sessão"
              icon="i-heroicons-plus"
              @click="showProgressModal = true"
            />
          </div>
          
          <!-- Background decoration -->
          <div class="absolute -right-4 -bottom-4 opacity-[0.03] dark:opacity-[0.05] pointer-events-none">
            <UIcon name="i-heroicons-chart-bar" class="w-48 h-48" />
          </div>
        </div>
      </div>

      <!-- Plano ativo -->
      <div class="rounded-2xl bg-white dark:bg-slate-900 border border-stone-200 dark:border-slate-800 shadow-sm p-6 flex flex-col">
        <div class="flex items-center gap-2 mb-6">
          <UIcon name="i-heroicons-book-open" class="text-primary-600 w-5 h-5" />
          <h2 class="font-bold text-slate-900 dark:text-white text-lg font-outfit">Plano de estudos</h2>
        </div>

        <div v-if="dashboard?.activePlan" class="space-y-6 flex-1 flex flex-col">
          <p class="text-sm font-semibold text-slate-700 dark:text-slate-300 leading-snug">{{ dashboard.activePlan.title }}</p>
          
          <div class="space-y-2">
            <div class="flex justify-between text-xs font-bold text-slate-500 dark:text-slate-400">
              <span>PROGRESSO</span>
              <span>{{ dashboard.activePlan.completionPercentage }}%</span>
            </div>
            <div class="h-2 w-full bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div 
                class="h-full bg-primary-600 rounded-full transition-all duration-1000"
                :style="`width: ${dashboard.activePlan.completionPercentage}%`"
              />
            </div>
          </div>
          
          <div class="flex items-center gap-2 px-3 py-2 bg-slate-50 dark:bg-slate-800/50 rounded-xl text-xs text-slate-500 dark:text-slate-400">
            <UIcon name="i-heroicons-calendar" class="w-4 h-4" />
            <span>{{ dashboard.activePlan.daysRemaining }} dias restantes</span>
          </div>
          
          <div class="mt-auto">
            <NuxtLink to="/study-plan">
              <UButton block variant="outline" size="md" label="Ver plano completo" class="rounded-xl" />
            </NuxtLink>
          </div>
        </div>

        <div v-else class="flex-1 flex flex-col items-center justify-center text-center py-4">
          <div class="w-16 h-16 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
            <UIcon name="i-heroicons-book-open" class="w-8 h-8 text-slate-300 dark:text-slate-600" />
          </div>
          <p class="text-sm text-slate-500 dark:text-slate-400 mb-6 px-4">Você ainda não tem um plano de estudos personalizado para seu concurso.</p>
          <NuxtLink to="/study-plan" class="w-full">
            <UButton block size="md" label="Gerar plano com IA" class="rounded-xl" />
          </NuxtLink>
        </div>
      </div>

    </div>

    <!-- Conquistas -->
    <div class="rounded-2xl bg-white dark:bg-slate-900 border border-stone-200 dark:border-slate-800 shadow-sm p-6">
      <div class="flex items-center justify-between mb-8">
        <h2 class="font-bold text-slate-900 dark:text-white text-lg font-outfit">Conquistas</h2>
        <UButton variant="ghost" size="xs" label="Ver todas" />
      </div>
      
      <div class="grid grid-cols-3 md:grid-cols-6 gap-6">
        <div
          v-for="achievement in dashboard?.achievements || []"
          :key="achievement.id"
          class="flex flex-col items-center group cursor-help"
        >
          <div
            class="relative w-16 h-16 rounded-2xl flex items-center justify-center mb-3 text-3xl transition-transform group-hover:scale-110 duration-300"
            :class="achievement.unlocked 
              ? 'bg-gradient-to-br from-primary-50 to-primary-100 dark:from-primary-950/20 dark:to-primary-900/30 ring-1 ring-primary-200 dark:ring-primary-800' 
              : 'bg-slate-50 dark:bg-slate-800/50 grayscale opacity-40 ring-1 ring-slate-200 dark:ring-slate-700'"
          >
            {{ achievement.icon }}
            <div v-if="!achievement.unlocked" class="absolute inset-0 flex items-center justify-center">
              <UIcon name="i-heroicons-lock-closed" class="w-4 h-4 text-slate-400 dark:text-slate-600 translate-y-4" />
            </div>
          </div>
          <p class="text-xs font-bold text-slate-700 dark:text-slate-300">{{ achievement.name }}</p>
        </div>
      </div>
    </div>

    <!-- Recomendação -->
    <div 
      v-if="dashboard?.nextSessionRecommendation" 
      class="rounded-2xl p-6 bg-gradient-to-r from-primary-600 to-indigo-600 text-white relative overflow-hidden shadow-xl shadow-primary-500/20"
    >
      <div class="relative z-10 flex flex-col md:flex-row items-center gap-6">
        <div class="w-16 h-16 bg-white/20 backdrop-blur-md rounded-2xl flex items-center justify-center flex-shrink-0 animate-bounce-slow">
          <UIcon name="i-heroicons-light-bulb" class="text-white w-10 h-10" />
        </div>
        <div class="text-center md:text-left">
          <p class="text-lg font-bold font-outfit mb-1">Recomendação do seu Mentor</p>
          <p class="text-primary-50/90 text-sm leading-relaxed max-w-2xl">{{ dashboard.nextSessionRecommendation }}</p>
        </div>
        <NuxtLink to="/chat" class="md:ml-auto w-full md:w-auto">
          <UButton 
            size="lg" 
            color="neutral" 
            variant="solid" 
            label="Estudar agora" 
            class="!text-primary-700 bg-white hover:bg-primary-50 font-bold px-8 rounded-xl w-full" 
          />
        </NuxtLink>
      </div>
      
      <!-- Decoration -->
      <div class="absolute top-0 right-0 -translate-y-1/2 translate-x-1/3 w-64 h-64 bg-white/10 rounded-full blur-3xl opacity-50" />
      <div class="absolute bottom-0 left-0 translate-y-1/2 -translate-x-1/3 w-48 h-48 bg-indigo-400/20 rounded-full blur-2xl opacity-50" />
    </div>

    <!-- Modal registrar progresso -->
    <UModal v-model:open="showProgressModal" :ui="{ content: 'sm:max-w-md' }">
      <template #content>
        <UCard class="dark:bg-slate-900">
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="font-bold text-slate-900 dark:text-white text-lg font-outfit">Registrar sessão de estudos</h3>
              <UButton color="neutral" variant="ghost" icon="i-heroicons-x-mark" size="sm" @click="showProgressModal = false" />
            </div>
          </template>
          
          <div class="space-y-6">
            <UFormField label="Quanto tempo você estudou?">
              <div class="relative">
                <UInput v-model="progressForm.minutesStudied" type="number" min="1" max="1440" class="pl-10" />
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span class="text-slate-400 text-sm">min</span>
                </div>
              </div>
            </UFormField>
            
            <UFormField label="Como foi seu desempenho hoje?">
              <div class="flex justify-between bg-slate-50 dark:bg-slate-800/50 p-4 rounded-2xl ring-1 ring-stone-100 dark:ring-slate-800">
                <button
                  v-for="i in 5"
                  :key="i"
                  class="flex flex-col items-center gap-1 transition-all duration-200"
                  :class="progressForm.moodRating === i ? 'scale-125' : 'opacity-40 grayscale hover:opacity-100 hover:grayscale-0'"
                  @click="progressForm.moodRating = i"
                >
                  <span class="text-3xl">{{ ['😞', '😕', '😐', '😊', '🤩'][i - 1] }}</span>
                  <span class="text-[10px] font-bold text-slate-500">{{ ['Ruim', 'Ok', 'Bom', 'Ótimo', 'Incrível'][i - 1] }}</span>
                </button>
              </div>
            </UFormField>
            
            <UFormField label="Alguma observação importante?">
              <UTextarea v-model="progressForm.notes" placeholder="Ex: Tive dificuldade em Crase hoje..." autoresize :rows="3" />
            </UFormField>
          </div>
          
          <template #footer>
            <div class="flex justify-end gap-3">
              <UButton variant="ghost" label="Cancelar" @click="showProgressModal = false" class="rounded-xl" />
              <UButton label="Salvar progresso" :loading="savingProgress" block class="flex-1 rounded-xl" @click="saveProgress" />
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
  if (hour < 12) return 'Bom dia! Pronto para focar nos estudos hoje?'
  if (hour < 18) return 'Boa tarde! Como estão indo os estudos?'
  return 'Boa noite! Que tal uma revisão rápida antes de dormir?'
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
    label: 'Sequência',
    value: `${dashboard.value?.currentStreakDays || 0}🔥`,
    icon: 'i-heroicons-fire',
    bg: 'bg-orange-100',
    color: 'text-orange-600',
  },
  {
    label: 'XP Acumulado',
    value: dashboard.value?.totalXp || 0,
    icon: 'i-heroicons-sparkles',
    bg: 'bg-yellow-100',
    color: 'text-yellow-600',
  },
  {
    label: 'Exercícios',
    value: dashboard.value?.totalExercisesDone || 0,
    icon: 'i-heroicons-check-badge',
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
  const days = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
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

<<<<<<< Updated upstream
onMounted(loadDashboard)
</script>

<style scoped>
.font-outfit {
  font-family: 'Outfit', sans-serif;
}

.animate-bounce-slow {
  animation: bounce 3s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(-5%);
    animation-timing-function: cubic-bezier(0.8, 0, 1, 1);
  }
  50% {
    transform: translateY(0);
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
  }
}
</style>
=======
onMounted(async () => {
  await loadDashboard()

  // Detect successful upgrade from Stripe
  const route = useRoute()
  if (route.query.upgrade === 'success') {
    toast.add({
      title: 'Upgrade realizado com sucesso! 🎉',
      description: 'Seu plano foi atualizado. Aproveite todos os recursos!',
      color: 'success',
    })
    // Clean URL
    const router = useRouter()
    router.replace({ path: '/dashboard', query: {} })
  }
})
</script>
>>>>>>> Stashed changes
