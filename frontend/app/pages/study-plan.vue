<template>
  <div class="space-y-6">

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">Plano de Estudos</h1>
        <p class="text-slate-500 mt-1">Seu cronograma personalizado gerado por IA</p>
      </div>
      <UButton
        label="Gerar novo plano"
        icon="i-heroicons-sparkles"
        :loading="generating"
        @click="generatePlan"
      />
    </div>

    <!-- Plano ativo -->
    <div v-if="activePlan">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <div>
              <h2 class="font-semibold text-slate-900">{{ activePlan.title }}</h2>
              <p class="text-sm text-slate-500 mt-1">{{ activePlan.description }}</p>
            </div>
            <UBadge label="Ativo" color="success" variant="subtle" />
          </div>
        </template>

        <!-- Progresso geral -->
        <div class="mb-6">
          <div class="flex justify-between text-sm mb-2">
            <span class="text-slate-600">Progresso geral</span>
            <span class="font-medium text-slate-900">{{ activePlan.completionPercentage }}%</span>
          </div>
          <UProgress :value="activePlan.completionPercentage" color="primary" size="md" />
          <div class="flex justify-between text-xs text-slate-400 mt-1">
            <span>Início: {{ formatDate(activePlan.startDate) }}</span>
            <span>Fim: {{ formatDate(activePlan.endDate) }}</span>
          </div>
        </div>

        <!-- Matérias -->
        <div v-if="activePlan.subjectsCovered?.length" class="mb-6">
          <p class="text-sm font-medium text-slate-700 mb-3">Matérias cobertas:</p>
          <div class="flex flex-wrap gap-2">
            <UBadge
              v-for="subject in activePlan.subjectsCovered"
              :key="subject"
              :label="subject"
              color="primary"
              variant="subtle"
            />
          </div>
        </div>

        <!-- Semanas do plano -->
        <div v-if="weeks.length" class="space-y-4">
          <p class="text-sm font-medium text-slate-700">Cronograma semanal:</p>
          <UAccordion :items="weeks">
            <template #default="{ item }">
              <div class="space-y-3 pb-4">
                <div
                  v-for="day in item.days"
                  :key="day.day"
                  class="p-3 bg-slate-50 rounded-lg"
                >
                  <div class="flex items-center gap-2 mb-2">
                    <UBadge :label="day.day" color="neutral" variant="outline" size="xs" />
                    <span class="text-xs text-slate-500">{{ day.duration_minutes }} min</span>
                    <UBadge
                      :label="day.priority"
                        :color="day.priority === 'alta' ? 'error' : day.priority === 'media' ? 'warning' : 'success'"
                      size="xs"
                      variant="subtle"
                    />
                    <div class="flex-1" />
                    <UButton
                      label="Registrar"
                      size="xs"
                      variant="outline"
                      @click="registerDayProgress(item.week, day)"
                    />
                  </div>
                  <ul class="space-y-1">
                    <li
                      v-for="topic in day.topics"
                      :key="topic"
                      class="text-sm text-slate-700 flex items-center gap-2"
                    >
                      <UCheckbox
                        :model-value="completedTopics[`${item.week}:${day.day}:${topic}`] || false"
                        @update:model-value="(v) => { completedTopics[`${item.week}:${day.day}:${topic}`] = !!v }"
                      />
                      <UIcon name="i-heroicons-book-open" class="w-3 h-3 text-primary-500 flex-shrink-0" />
                      <span>{{ topic }}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </template>
          </UAccordion>
        </div>
      </UCard>
    </div>

    <!-- Sem plano -->
    <UCard v-else>
      <div class="text-center py-16">
        <UIcon name="i-heroicons-book-open" class="w-16 h-16 text-slate-300 mx-auto mb-4" />
        <h3 class="font-semibold text-slate-900 mb-2">Nenhum plano ativo</h3>
        <p class="text-slate-500 text-sm mb-6 max-w-sm mx-auto">
          Clique em "Gerar novo plano" para criar um cronograma personalizado baseado no seu perfil
        </p>
        <UButton
          label="Gerar plano com IA"
          icon="i-heroicons-sparkles"
          size="lg"
          :loading="generating"
          @click="generatePlan"
        />
      </div>
    </UCard>

    <!-- Histórico de planos -->
    <UCard v-if="allPlans.length > 1">
      <template #header>
        <h2 class="font-semibold text-slate-900">Planos anteriores</h2>
      </template>
      <div class="space-y-2">
        <div
          v-for="plan in allPlans.filter(p => !p.isActive)"
          :key="plan.id"
          class="flex items-center justify-between p-3 rounded-lg border border-slate-200"
        >
          <div>
            <p class="text-sm font-medium text-slate-700">{{ plan.title }}</p>
            <p class="text-xs text-slate-400">{{ formatDate(plan.createdAt) }}</p>
          </div>
          <UBadge :label="plan.status" color="neutral" variant="outline" size="xs" />
        </div>
      </div>
    </UCard>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default', middleware: 'auth' })

const { get, post } = useApi()
const toast = useToast()

type PlanDay = {
  day: string
  duration_minutes: number
  priority: 'alta' | 'media' | 'baixa' | string
  topics: string[]
}

type PlanWeek = {
  week: number
  focus?: string
  days?: PlanDay[]
}

type PlanContent = {
  weeks?: PlanWeek[]
}

type ActivePlan = {
  id: string
  title: string
  description: string
  completionPercentage: number
  startDate: string
  endDate: string
  subjectsCovered?: string[]
  planContent?: PlanContent
}

type PreviousPlan = {
  id: string
  title: string
  createdAt: string
  status: string
  isActive: boolean
}

const activePlan = ref<ActivePlan | null>(null)
const allPlans = ref<PreviousPlan[]>([])
const generating = ref(false)

// Track de tópicos marcados como concluídos por dia.
// key = `${week}:${day}:${topic}`
const completedTopics = reactive<Record<string, boolean>>({})

const weeks = computed(() => {
  const content = activePlan.value?.planContent
  if (!content?.weeks) return []
  return content.weeks.map((w) => ({
    label: `Semana ${w.week} — ${w.focus || ''}`,
    days: w.days ?? [],
    week: w.week,
  }))
})

const topicKey = (week: number, day: string, topic: string) => `${week}:${day}:${topic}`

const registerDayProgress = async (week: number, day: PlanDay) => {
  if (!activePlan.value?.id) {
    toast.add({ title: 'Plano ativo inválido', color: 'warning' })
    return
  }

  const selectedTopics = (day.topics || []).filter((t) => completedTopics[topicKey(week, day.day, t)])
  if (!selectedTopics.length) {
    toast.add({ title: 'Marque ao menos 1 tópico como concluído', color: 'warning' })
    return
  }

  try {
    await post('/dashboard/progress', {
      minutesStudied: Math.max(0, Number(day.duration_minutes || 0)),
      topicsCompleted: selectedTopics,
      studyPlanId: activePlan.value.id,
    })
    toast.add({ title: 'Progresso registrado! +XP', color: 'success' })
    await loadPlans()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao registrar progresso'
    toast.add({ title: 'Falha ao registrar', description: message, color: 'error' })
  }
}

const generatePlan = async () => {
  generating.value = true
  try {
    activePlan.value = await post<ActivePlan>('/study-plans/generate', {})
    toast.add({ title: 'Plano gerado com sucesso! 🎉', color: 'success' })
    await loadPlans()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao gerar plano'
    toast.add({ title: 'Erro ao gerar plano', description: message, color: 'error' })
  } finally {
    generating.value = false
  }
}

const loadPlans = async () => {
  try {
    allPlans.value = await get<PreviousPlan[]>('/study-plans')
    // O backend retorna objetos diferentes para planos ativos vs anteriores,
    // mas quando `isActive` é true, esperamos que o payload seja compatível com `ActivePlan`.
    activePlan.value = (allPlans.value.find((p) => p.isActive) as ActivePlan | undefined) ?? null
  } catch (error) {
    console.error('Error loading plans:', error)
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('pt-BR')
}

onMounted(loadPlans)
</script>