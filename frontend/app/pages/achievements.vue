<template>
  <div class="space-y-6">

    <div>
      <h1 class="text-2xl font-bold text-slate-900">Conquistas</h1>
      <p class="text-slate-500 mt-1">Seu progresso e medalhas desbloqueadas</p>
    </div>

    <!-- XP e nível -->
    <UCard class="bg-gradient-to-r from-primary-600 to-primary-700 text-white">
      <div class="flex items-center gap-6">
        <div class="text-center">
          <p class="text-4xl font-bold">{{ dashboard?.totalXp || 0 }}</p>
          <p class="text-primary-200 text-sm">XP Total</p>
        </div>
        <div class="flex-1">
          <div class="flex justify-between text-sm mb-2">
            <span>Nível {{ currentLevel }}</span>
            <span>{{ dashboard?.totalXp || 0 }} / {{ nextLevelXp }} XP</span>
          </div>
          <div class="w-full bg-primary-500 rounded-full h-3">
            <div
              class="bg-white rounded-full h-3 transition-all"
              :style="`width: ${levelProgress}%`"
            />
          </div>
          <p class="text-primary-200 text-xs mt-1">{{ nextLevelXp - (dashboard?.totalXp || 0) }} XP para o próximo nível</p>
        </div>
      </div>
    </UCard>

    <!-- Stats rápidas -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <UCard v-for="stat in stats" :key="stat.label" class="text-center">
        <p class="text-3xl mb-1">{{ stat.emoji }}</p>
        <p class="text-xl font-bold text-slate-900">{{ stat.value }}</p>
        <p class="text-xs text-slate-500">{{ stat.label }}</p>
      </UCard>
    </div>

    <!-- Conquistas -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Medalhas</h2>
      </template>
      <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
        <div
          v-for="achievement in dashboard?.achievements || []"
          :key="achievement.id"
          class="flex items-center gap-3 p-4 rounded-xl border transition-colors"
          :class="achievement.unlocked
            ? 'border-primary-200 bg-primary-50'
            : 'border-slate-200 bg-slate-50 opacity-50'"
        >
          <span class="text-3xl">{{ achievement.icon }}</span>
          <div>
            <p class="font-medium text-sm text-slate-900">{{ achievement.name }}</p>
            <p class="text-xs text-slate-500">{{ achievement.description }}</p>
            <UBadge
              v-if="achievement.unlocked"
              label="Desbloqueada"
              color="success"
              size="xs"
              variant="subtle"
              class="mt-1"
            />
          </div>
        </div>
      </div>
    </UCard>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default', middleware: 'auth' })

const { get } = useApi()

type Achievement = {
  id: string
  icon: string
  name: string
  description: string
  unlocked: boolean
}

type DashboardResponse = {
  totalXp: number
  currentStreakDays: number
  totalMinutesStudied: number
  totalExercisesDone: number
  achievements: Achievement[]
}

const dashboard = ref<DashboardResponse | null>(null)

const currentLevel = computed(() => {
  const xp = dashboard.value?.totalXp || 0
  return Math.floor(xp / 500) + 1
})

const nextLevelXp = computed(() => currentLevel.value * 500)

const levelProgress = computed(() => {
  const xp = dashboard.value?.totalXp || 0
  const levelStart = (currentLevel.value - 1) * 500
  return Math.min(((xp - levelStart) / 500) * 100, 100)
})

const stats = computed(() => [
  { emoji: '🔥', value: dashboard.value?.currentStreakDays || 0, label: 'Dias seguidos' },
  { emoji: '⏱️', value: `${Math.floor((dashboard.value?.totalMinutesStudied || 0) / 60)}h`, label: 'Estudadas' },
  { emoji: '📝', value: dashboard.value?.totalExercisesDone || 0, label: 'Exercícios' },
  {
    emoji: '🏆',
    value: dashboard.value?.achievements?.filter((a) => a.unlocked)?.length ?? 0,
    label: 'Medalhas'
  },
])

onMounted(async () => {
  dashboard.value = await get<DashboardResponse>('/dashboard')
})
</script>