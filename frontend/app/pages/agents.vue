<template>
  <div class="space-y-6">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">Central de Agentes</h1>
        <p class="text-slate-500 mt-1">
          Use o agente de contato, pesquisa de materiais e planejamento de estudos em um só lugar.
        </p>
      </div>
      <NuxtLink to="/study-plan">
        <UButton label="Ver planos" icon="i-heroicons-book-open" variant="outline" />
      </NuxtLink>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <UCard>
        <template #header>
          <h2 class="font-semibold text-slate-900">1) Agente de contato</h2>
          <p class="text-xs text-slate-500 mt-1">Faz a ponte entre você e os agentes internos.</p>
        </template>
        <div class="space-y-3">
          <UTextarea
            v-model="liaisonForm.content"
            placeholder="Ex.: preciso de um plano para o ENEM em 4 meses"
            :rows="4"
            autoresize
          />
          <UButton
            label="Enviar para agente de contato"
            block
            :loading="liaisonPending"
            @click="sendToLiaison"
          />
          <div v-if="liaisonResponse" class="p-3 rounded-lg bg-slate-50 text-sm space-y-1">
            <p><span class="font-medium">Roteado para:</span> {{ liaisonResponse.routedAgent }}</p>
            <p><span class="font-medium">Motivo:</span> {{ liaisonResponse.routingReason }}</p>
            <p class="text-slate-600 whitespace-pre-wrap">{{ liaisonResponse.chat.assistantMessage.content }}</p>
          </div>
        </div>
      </UCard>

      <UCard>
        <template #header>
          <h2 class="font-semibold text-slate-900">2) Agente de pesquisa</h2>
          <p class="text-xs text-slate-500 mt-1">Busca materiais validados por tema.</p>
        </template>
        <div class="space-y-3">
          <UInput v-model="researchTopic" placeholder="Ex.: matemática ENEM" />
          <UButton
            label="Buscar materiais"
            block
            :loading="researchPending"
            @click="loadMaterials"
          />
          <div v-if="researchResponse" class="p-3 rounded-lg bg-slate-50 text-sm space-y-2">
            <p><span class="font-medium">Tema:</span> {{ researchResponse.topic }}</p>
            <p><span class="font-medium">Critérios:</span> {{ researchResponse.validatedCriteria }}</p>
            <p class="text-slate-600 whitespace-pre-wrap">{{ researchResponse.curatedMaterials }}</p>
          </div>
        </div>
      </UCard>

      <UCard>
        <template #header>
          <h2 class="font-semibold text-slate-900">3) Agente planejador</h2>
          <p class="text-xs text-slate-500 mt-1">Sugere cursos, métodos e estratégia semanal.</p>
        </template>
        <div class="space-y-3">
          <UInput v-model="plannerGoal" placeholder="Ex.: passar em concurso de TI" />
          <UButton
            label="Gerar orientação"
            block
            :loading="plannerPending"
            @click="loadGuidance"
          />
          <UButton
            label="Gerar plano de estudo completo"
            block
            color="primary"
            variant="outline"
            :loading="studyPlanPending"
            @click="generateStudyPlan"
          />
          <div v-if="plannerResponse" class="p-3 rounded-lg bg-slate-50 text-sm space-y-2">
            <p><span class="font-medium">Objetivo:</span> {{ plannerResponse.goal }}</p>
            <p class="text-slate-600 whitespace-pre-wrap">{{ plannerResponse.weeklyPlanGuidance }}</p>
          </div>
          <div v-if="studyPlanFeedback" class="text-xs text-primary-700 bg-primary-50 rounded px-3 py-2">
            {{ studyPlanFeedback }}
          </div>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default',
  middleware: 'auth',
})

const { get, post } = useApi()
const toast = useToast()

type LiaisonResponse = {
  routedAgent: string
  routingReason: string
  chat: {
    assistantMessage: {
      content: string
    }
  }
}

type ResearchResponse = {
  topic: string
  validatedCriteria: string
  curatedMaterials: string
}

type PlannerResponse = {
  goal: string
  recommendedCourses: string
  studyMethods: string
  weeklyPlanGuidance: string
}

const liaisonForm = reactive({
  content: '',
  sessionId: '',
})
const researchTopic = ref('')
const plannerGoal = ref('')

const liaisonPending = ref(false)
const researchPending = ref(false)
const plannerPending = ref(false)
const studyPlanPending = ref(false)

const liaisonResponse = ref<LiaisonResponse | null>(null)
const researchResponse = ref<ResearchResponse | null>(null)
const plannerResponse = ref<PlannerResponse | null>(null)
const studyPlanFeedback = ref('')

const sendToLiaison = async () => {
  if (!liaisonForm.content.trim()) {
    toast.add({ title: 'Digite uma mensagem', color: 'warning' })
    return
  }
  liaisonPending.value = true
  try {
    liaisonResponse.value = await post<LiaisonResponse>('/agents/liaison/message', {
      content: liaisonForm.content.trim(),
      sessionId: liaisonForm.sessionId || undefined,
    })
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro no agente de contato'
    toast.add({ title: 'Falha no contato', description: message, color: 'error' })
  } finally {
    liaisonPending.value = false
  }
}

const loadMaterials = async () => {
  if (!researchTopic.value.trim()) {
    toast.add({ title: 'Digite um tema para pesquisa', color: 'warning' })
    return
  }
  researchPending.value = true
  try {
    researchResponse.value = await get<ResearchResponse>(`/agents/research/materials?topic=${encodeURIComponent(researchTopic.value.trim())}`)
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro no agente de pesquisa'
    toast.add({ title: 'Falha na pesquisa', description: message, color: 'error' })
  } finally {
    researchPending.value = false
  }
}

const loadGuidance = async () => {
  if (!plannerGoal.value.trim()) {
    toast.add({ title: 'Digite um objetivo', color: 'warning' })
    return
  }
  plannerPending.value = true
  try {
    plannerResponse.value = await get<PlannerResponse>(`/agents/planner/guidance?goal=${encodeURIComponent(plannerGoal.value.trim())}`)
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro no agente planejador'
    toast.add({ title: 'Falha no planejamento', description: message, color: 'error' })
  } finally {
    plannerPending.value = false
  }
}

const generateStudyPlan = async () => {
  studyPlanPending.value = true
  studyPlanFeedback.value = ''
  try {
    await post('/agents/planner/study-plan', {})
    studyPlanFeedback.value = 'Plano gerado com sucesso. Abra "Plano de Estudos" para visualizar os detalhes.'
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao gerar plano'
    toast.add({ title: 'Falha ao gerar plano', description: message, color: 'error' })
  } finally {
    studyPlanPending.value = false
  }
}
</script>
