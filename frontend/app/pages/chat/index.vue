<template>
  <div class="flex h-[calc(100vh-8rem)] gap-4">

    <!-- Sidebar de sessões -->
    <div class="hidden lg:flex flex-col w-64 flex-shrink-0">
      <UCard class="flex-1 flex flex-col overflow-hidden">
        <template #header>
          <div class="flex items-center justify-between">
            <h2 class="font-semibold text-slate-900 text-sm">Conversas</h2>
            <UButton
              icon="i-heroicons-plus"
              size="xs"
              variant="ghost"
              @click="newChat"
            />
          </div>
        </template>

        <div class="flex-1 overflow-y-auto space-y-1 -mx-4 px-4">
          <button
            v-for="session in sessions"
            :key="session.id"
            class="w-full text-left px-3 py-2.5 rounded-lg text-sm transition-colors"
            :class="currentSessionId === session.id
              ? 'bg-primary-50 text-primary-700'
              : 'text-slate-600 hover:bg-slate-100'"
            @click="loadSession(session.id)"
          >
            <p class="font-medium truncate">{{ session.title }}</p>
            <p class="text-xs opacity-60 mt-0.5">{{ formatDate(session.updatedAt) }}</p>
          </button>

          <div v-if="sessions.length === 0" class="text-center py-8 text-slate-400 text-sm">
            Nenhuma conversa ainda
          </div>
        </div>
      </UCard>
    </div>

    <!-- Chat principal -->
    <UCard class="flex-1 flex flex-col overflow-hidden">

      <!-- Header do chat -->
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 bg-primary-600 rounded-full flex items-center justify-center">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-5 h-5" />
          </div>
          <div>
            <p class="font-semibold text-slate-900 text-sm">MentorIA</p>
            <p class="text-xs text-green-500">● Online</p>
          </div>
          <div class="ml-auto flex items-center gap-2">
            <UBadge
              v-if="remainingMessages !== null && authStore.user?.planType === 'FREE'"
              :label="`${remainingMessages} msgs restantes`"
              :color="remainingMessages <= 3 ? 'red' : 'gray'"
              variant="subtle"
              size="xs"
            />
          </div>
        </div>
      </template>

      <!-- Mensagens -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto space-y-4 p-4">

        <!-- Mensagem de boas vindas -->
        <div v-if="messages.length === 0" class="text-center py-12">
          <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <UIcon name="i-heroicons-academic-cap" class="text-primary-600 w-8 h-8" />
          </div>
          <h3 class="font-semibold text-slate-900 mb-2">Olá! Sou seu MentorIA 👋</h3>
          <p class="text-slate-500 text-sm max-w-sm mx-auto">
            Estou aqui para te ajudar nos estudos. Pode me perguntar sobre qualquer matéria do seu concurso!
          </p>
          <div class="flex flex-wrap gap-2 justify-center mt-6">
            <UButton
              v-for="suggestion in suggestions"
              :key="suggestion"
              :label="suggestion"
              variant="outline"
              color="gray"
              size="xs"
              @click="sendSuggestion(suggestion)"
            />
          </div>
        </div>

        <!-- Mensagens -->
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="flex gap-3"
          :class="msg.role === 'USER' ? 'flex-row-reverse' : ''"
        >
          <!-- Avatar -->
          <div class="flex-shrink-0">
            <div
              v-if="msg.role === 'ASSISTANT'"
              class="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center"
            >
              <UIcon name="i-heroicons-academic-cap" class="text-white w-4 h-4" />
            </div>
            <UAvatar v-else :alt="authStore.user?.name" size="sm" />
          </div>

          <!-- Conteúdo -->
          <div
            class="max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed"
            :class="msg.role === 'USER'
              ? 'bg-primary-600 text-white rounded-tr-sm'
              : 'bg-slate-100 text-slate-800 rounded-tl-sm'"
          >
            <p class="whitespace-pre-wrap">{{ msg.content }}</p>
            <p
              class="text-xs mt-1 opacity-60"
              :class="msg.role === 'USER' ? 'text-right' : ''"
            >
              {{ formatTime(msg.createdAt) }}
            </p>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="isTyping" class="flex gap-3">
          <div class="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center flex-shrink-0">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-4 h-4" />
          </div>
          <div class="bg-slate-100 rounded-2xl rounded-tl-sm px-4 py-3">
            <div class="flex gap-1 items-center h-4">
              <span class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 0ms" />
              <span class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 150ms" />
              <span class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 300ms" />
            </div>
          </div>
        </div>

      </div>

      <!-- Aviso de limite -->
      <div v-if="limitReached" class="px-4 py-3 bg-red-50 border-t border-red-200">
        <div class="flex items-center gap-3">
          <UIcon name="i-heroicons-exclamation-triangle" class="text-red-500 w-5 h-5 flex-shrink-0" />
          <p class="text-sm text-red-700">Você atingiu o limite diário. Faça upgrade para continuar!</p>
          <NuxtLink to="/pricing" class="ml-auto">
            <UButton size="xs" label="Fazer upgrade" color="red" />
          </NuxtLink>
        </div>
      </div>

      <!-- Input -->
      <template #footer>
        <div class="flex gap-3 items-end">
          <UTextarea
            v-model="inputMessage"
            placeholder="Digite sua mensagem..."
            :rows="1"
            autoresize
            :maxrows="5"
            class="flex-1"
            :disabled="limitReached || isTyping"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <UButton
            icon="i-heroicons-paper-airplane"
            :loading="isTyping"
            :disabled="!inputMessage.trim() || limitReached"
            @click="sendMessage"
          />
        </div>
        <p class="text-xs text-slate-400 mt-2 text-center">
          Enter para enviar • Shift+Enter para nova linha
        </p>
      </template>
    </UCard>

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

const messages = ref<any[]>([])
const sessions = ref<any[]>([])
const currentSessionId = ref<string | null>(null)
const inputMessage = ref('')
const isTyping = ref(false)
const limitReached = ref(false)
const remainingMessages = ref<number | null>(null)
const messagesContainer = ref<HTMLElement | null>(null)

const suggestions = [
  'Me explica raciocínio lógico',
  'Como estudar português para concursos?',
  'Quais as matérias mais cobradas no INSS?',
  'Crie um resumo sobre direito administrativo',
]

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value || limitReached.value) return

  const content = inputMessage.value.trim()
  inputMessage.value = ''

  // Adiciona mensagem do usuário localmente
  messages.value.push({
    id: Date.now(),
    role: 'USER',
    content,
    createdAt: new Date().toISOString(),
  })

  scrollToBottom()
  isTyping.value = true

  try {
    const response = await post<any>('/chat/message', {
      content,
      sessionId: currentSessionId.value,
    })

    currentSessionId.value = response.sessionId
    remainingMessages.value = response.remainingMessages
    limitReached.value = response.limitReached

    messages.value.push(response.assistantMessage)
    await loadSessions()
  } catch (error: any) {
    if (error.message?.includes('limite')) {
      limitReached.value = true
    } else {
      toast.add({ title: 'Erro ao enviar mensagem', description: error.message, color: 'red' })
    }
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const sendSuggestion = (text: string) => {
  inputMessage.value = text
  sendMessage()
}

const newChat = () => {
  currentSessionId.value = null
  messages.value = []
  limitReached.value = false
}

const loadSession = async (sessionId: string) => {
  currentSessionId.value = sessionId
  try {
    messages.value = await get(`/chat/sessions/${sessionId}/messages`)
    scrollToBottom()
  } catch (error) {
    toast.add({ title: 'Erro ao carregar conversa', color: 'red' })
  }
}

const loadSessions = async () => {
  try {
    sessions.value = await get('/chat/sessions')
  } catch (error) {
    console.error('Error loading sessions:', error)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

const formatTime = (date: string) => {
  return new Date(date).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

onMounted(loadSessions)
</script>