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
            <UBadge label="Ativo" color="green" variant="subtle" />
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
                    <UBadge :label="day.day" color="gray" variant="outline" size="xs" />
                    <span class="text-xs text-slate-500">{{ day.duration_minutes }} min</span>
                    <UBadge
                      :label="day.priority"
                      :color="day.priority === 'alta' ? 'red' : day.priority === 'media' ? 'yellow' : 'green'"
                      size="xs"
                      variant="subtle"
                    />
                  </div>
                  <ul class="space-y-1">
                    <li
                      v-for="topic in day.topics"
                      :key="topic"
                      class="text-sm text-slate-700 flex items-center gap-2"
                    >
                      <UIcon name="i-heroicons-book-open" class="w-3 h-3 text-primary-500 flex-shrink-0" />
                      {{ topic }}
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
          <UBadge :label="plan.status" color="gray" variant="outline" size="xs" />
        </div>
      </div>
    </UCard>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default', middleware: 'auth' })

const { get, post } = useApi()
const toast = useToast()

const activePlan = ref<any>(null)
const allPlans = ref<any[]>([])
const generating = ref(false)

const weeks = computed(() => {
  const content = activePlan.value?.planContent
  if (!content?.weeks) return []
  return content.weeks.map((w: any) => ({
    label: `Semana ${w.week} — ${w.focus || ''}`,
    days: w.days || [],
  }))
})

const generatePlan = async () => {
  generating.value = true
  try {
    activePlan.value = await post('/study-plans/generate', {})
    toast.add({ title: 'Plano gerado com sucesso! 🎉', color: 'green' })
    await loadPlans()
  } catch (error: any) {
    toast.add({ title: 'Erro ao gerar plano', description: error.message, color: 'red' })
  } finally {
    generating.value = false
  }
}

const loadPlans = async () => {
  try {
    allPlans.value = await get('/study-plans')
    activePlan.value = allPlans.value.find((p: any) => p.isActive) || null
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