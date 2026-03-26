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
            <p :class="connected ? 'text-green-500' : 'text-red-500'" class="text-xs">
              ● {{ connected ? 'Conectado' : 'Desconectado' }}
            </p>
          </div>
          <div class="ml-auto flex items-center gap-2">
            <UBadge
              v-if="remainingMessages !== null && authStore.user?.planType === 'FREE'"
              :label="`${remainingMessages} msgs restantes`"
              :color="remainingMessages <= 3 ? 'error' : 'neutral'"
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
              color="neutral"
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
            class="max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed markdown-content"
            :class="msg.role === 'USER'
              ? 'bg-primary-600 text-white rounded-tr-sm'
              : 'bg-slate-100 text-slate-800 rounded-tl-sm shadow-sm'"
          >
            <div v-if="msg.role === 'ASSISTANT'" v-html="renderMarkdown(msg.content)" />
            <p v-else class="whitespace-pre-wrap">{{ msg.content }}</p>
            
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
            <UButton size="xs" label="Fazer upgrade" color="error" />
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
            :disabled="limitReached || isTyping || !connected"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <UButton
            icon="i-heroicons-paper-airplane"
            :loading="isTyping"
            :disabled="!inputMessage.trim() || limitReached || !connected"
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
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import MarkdownIt from 'markdown-it'

definePageMeta({
  layout: 'default',
  middleware: 'auth',
})

const authStore = useAuthStore()
const { get, post, apiBase } = useApi()
const toast = useToast()
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

type ChatRole = 'USER' | 'ASSISTANT'

type ChatMessage = {
  id: number | string
  role: ChatRole
  content: string
  createdAt: string
}

type ChatSession = {
  id: string
  title: string
  messageCount: number
  updatedAt: string
}

type SendMessageResponse = {
  sessionId: string
  remainingMessages: number
  limitReached: boolean
  assistantMessage: ChatMessage
}

const messages = ref<ChatMessage[]>([])
const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<string | null>(null)
const inputMessage = ref('')
const isTyping = ref(false)
const limitReached = ref(false)
const remainingMessages = ref<number | null>(null)
const messagesContainer = ref<HTMLElement | null>(null)
const connected = ref(false)
let stompClient: any = null

const suggestions = [
  'Me explica raciocínio lógico',
  'Como estudar português para concursos?',
  'Quais as matérias mais cobradas no INSS?',
  'Crie um resumo sobre direito administrativo',
]

const renderMarkdown = (content: string) => {
  return md.render(content)
}

const connectWebSocket = () => {
  const socket = new SockJS(apiBase + '/ws')
  stompClient = Stomp.over(socket)
  stompClient.debug = () => {} // Disable debug logs

  stompClient.connect({}, (frame: any) => {
    connected.value = true
    console.log('Connected: ' + frame)
    
    if (currentSessionId.value) {
      subscribeToSession(currentSessionId.value)
    }
  }, (error: any) => {
    connected.value = false
    console.error('STOMP error:', error)
    setTimeout(connectWebSocket, 5000) // Retry
  })
}

const subscribeToSession = (sessionId: string) => {
  if (stompClient && stompClient.connected) {
    stompClient.subscribe('/topic/chat/' + sessionId, (message: any) => {
      const response = JSON.parse(message.body) as SendMessageResponse
      
      // Update session info
      remainingMessages.value = response.remainingMessages
      limitReached.value = response.limitReached
      
      // Add assistant message if not already added
      if (!messages.value.find(m => m.id === response.assistantMessage.id)) {
        messages.value.push(response.assistantMessage)
        isTyping.value = false
        scrollToBottom()
        loadSessions()
      }
    })
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value || limitReached.value || !connected.value) return

  const content = inputMessage.value.trim()
  inputMessage.value = ''

  // Adiciona mensagem do usuário localmente
  const userMsgId = Date.now()
  messages.value.push({
    id: userMsgId,
    role: 'USER',
    content,
    createdAt: new Date().toISOString(),
  })

  scrollToBottom()
  isTyping.value = true

  if (stompClient && stompClient.connected) {
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
      content,
      sessionId: currentSessionId.value
    }))
  } else {
    // Fallback para REST se o WebSocket falhar
    try {
      const response = await post<SendMessageResponse>('/chat/message', {
        content,
        sessionId: currentSessionId.value,
      })
      
      // If session changed, subscribe to new one
      if (currentSessionId.value !== response.sessionId) {
        currentSessionId.value = response.sessionId
        subscribeToSession(response.sessionId)
      }

      remainingMessages.value = response.remainingMessages
      limitReached.value = response.limitReached

      messages.value.push(response.assistantMessage)
      await loadSessions()
    } catch (error: any) {
      if (error.message?.includes('limite')) {
        limitReached.value = true
      } else {
        toast.add({ title: 'Erro ao enviar mensagem', color: 'error' })
      }
    } finally {
      isTyping.value = false
      scrollToBottom()
    }
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
  subscribeToSession(sessionId)
  try {
    const response = await get<any>(`/chat/sessions/${sessionId}/messages`)
    messages.value = response
    scrollToBottom()
  } catch {
    toast.add({ title: 'Erro ao carregar conversa', color: 'error' })
  }
}

const loadSessions = async () => {
  try {
    const response = await get<any>('/chat/sessions')
    sessions.value = response
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

onMounted(() => {
  loadSessions()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.disconnect()
  }
})

watch(currentSessionId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    subscribeToSession(newId)
  }
})
</script>

<style>
.markdown-content h1 { @apply text-xl font-bold mb-2; }
.markdown-content h2 { @apply text-lg font-bold mb-2; }
.markdown-content h3 { @apply text-base font-bold mb-1; }
.markdown-content p { @apply mb-2 last:mb-0; }
.markdown-content ul { @apply list-disc ml-5 mb-2; }
.markdown-content ol { @apply list-decimal ml-5 mb-2; }
.markdown-content code { @apply bg-slate-200 px-1 rounded font-mono text-xs; }
.markdown-content pre { @apply bg-slate-900 text-white p-3 rounded-lg overflow-x-auto mb-2 text-xs; }
.markdown-content blockquote { @apply border-l-4 border-primary-500 pl-4 py-1 italic mb-2; }
.markdown-content table { @apply w-full border-collapse mb-2; }
.markdown-content th, .markdown-content td { @apply border border-slate-300 p-2 text-left; }
.markdown-content th { @apply bg-slate-100; }
</style>