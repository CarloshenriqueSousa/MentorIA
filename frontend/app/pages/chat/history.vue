<template>
  <div class="space-y-6">

    <div>
      <h1 class="text-2xl font-bold text-slate-900">Histórico de Conversas</h1>
      <p class="text-slate-500 mt-1">Todas as suas conversas com o MentorIA</p>
    </div>

    <UCard>
      <div v-if="loading" class="flex justify-center py-12">
        <UIcon name="i-heroicons-arrow-path" class="w-8 h-8 text-slate-400 animate-spin" />
      </div>

      <div v-else-if="sessions.length === 0" class="text-center py-12">
        <UIcon name="i-heroicons-chat-bubble-left-right" class="w-12 h-12 text-slate-300 mx-auto mb-3" />
        <p class="text-slate-500">Nenhuma conversa ainda</p>
        <NuxtLink to="/chat" class="mt-4 inline-block">
          <UButton label="Iniciar primeira conversa" />
        </NuxtLink>
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="flex items-center gap-4 p-4 rounded-xl border border-slate-200 hover:border-primary-300 hover:bg-primary-50 transition-colors cursor-pointer"
          @click="goToChat(session.id)"
        >
          <div class="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center flex-shrink-0">
            <UIcon name="i-heroicons-chat-bubble-left-right" class="text-primary-600 w-5 h-5" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-medium text-slate-900 truncate">{{ session.title }}</p>
            <p class="text-sm text-slate-400">{{ session.messageCount }} mensagens</p>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-xs text-slate-400">{{ formatDate(session.updatedAt) }}</p>
          </div>
          <UButton
            icon="i-heroicons-trash"
            variant="ghost"
              color="error"
            size="xs"
            @click.stop="deleteSession(session.id)"
          />
        </div>
      </div>
    </UCard>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'default', middleware: 'auth' })

const { get, del } = useApi()
const router = useRouter()
const toast = useToast()

type ChatSession = {
  id: string
  title: string
  messageCount: number
  updatedAt: string
}

const sessions = ref<ChatSession[]>([])
const loading = ref(true)

const loadSessions = async () => {
  try {
    sessions.value = await get<ChatSession[]>('/chat/sessions')
  } catch {
    toast.add({ title: 'Erro ao carregar histórico', color: 'error' })
  } finally {
    loading.value = false
  }
}

const goToChat = (sessionId: string) => {
  router.push(`/chat?session=${sessionId}`)
}

const deleteSession = async (sessionId: string) => {
  try {
    await del(`/chat/sessions/${sessionId}`)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    toast.add({ title: 'Conversa excluída', color: 'success' })
  } catch {
    toast.add({ title: 'Erro ao excluir', color: 'error' })
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(loadSessions)
</script>