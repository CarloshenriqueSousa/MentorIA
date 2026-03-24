<template>
  <div class="space-y-8">
    <div>
      <UBadge label="Admin" color="error" variant="subtle" class="mb-2" />
      <h1 class="text-2xl font-bold text-slate-900">Painel de agentes</h1>
      <p class="text-slate-500 mt-1">
        Orquestração dos três agentes de IA: pesquisa de materiais, ponte usuário/backend e planos de aula.
      </p>
      <p v-if="overview?.environment" class="text-xs text-slate-400 mt-2">
        Ambiente: {{ overview.environment }}
      </p>
    </div>

    <UAlert
      v-if="error"
      color="error"
      variant="subtle"
      title="Não foi possível carregar o painel"
      :description="error"
    />

    <div v-else-if="pending" class="flex justify-center py-16">
      <UIcon name="i-heroicons-arrow-path" class="w-8 h-8 animate-spin text-primary-500" />
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <UCard v-for="agent in overview?.agents" :key="agent.id" class="flex flex-col">
        <template #header>
          <div class="flex items-start justify-between gap-2">
            <div>
              <h2 class="font-semibold text-slate-900">{{ agent.name }}</h2>
              <p class="text-xs text-slate-500 mt-1">{{ agent.description }}</p>
            </div>
            <UBadge
              :label="statusLabel(agent.status)"
              :color="statusColor(agent.status)"
              variant="subtle"
              size="sm"
            />
          </div>
        </template>

        <p class="text-sm text-slate-600 flex-1">{{ agent.detail }}</p>

        <template #footer>
          <p class="text-xs text-slate-500">
            <span class="font-medium text-slate-700">Integração:</span>
            {{ agent.integrationHint }}
          </p>
        </template>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default',
  middleware: ['auth', 'admin'],
})

type Agent = {
  id: string
  name: string
  description: string
  status: string
  detail: string
  integrationHint: string
}

type Overview = {
  environment: string
  agents: Agent[]
}

const { get } = useApi()
const error = ref<string | null>(null)
const pending = ref(true)
const overview = ref<Overview | null>(null)

function statusLabel(s: string) {
  const map: Record<string, string> = {
    ONLINE: 'Online',
    IDLE: 'Ocioso',
    BUSY: 'Ocupado',
    ERROR: 'Erro',
  }
  return map[s] || s
}

function statusColor(s: string): 'success' | 'neutral' | 'warning' | 'error' {
  if (s === 'ONLINE') return 'success'
  if (s === 'BUSY') return 'warning'
  if (s === 'ERROR') return 'error'
  return 'neutral'
}

onMounted(async () => {
  try {
    overview.value = await get<Overview>('/admin/overview')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Erro desconhecido'
  } finally {
    pending.value = false
  }
})
</script>
