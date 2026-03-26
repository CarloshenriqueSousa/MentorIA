<template>
  <div class="text-center">
    <!-- Step indicator -->
    <div class="flex items-center justify-center gap-2 mb-8">
      <div
        v-for="(step, i) in steps"
        :key="i"
        class="flex items-center gap-2"
      >
        <div
          :class="[
            'w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-300',
            i <= 1
              ? 'bg-primary-600 text-white shadow-md shadow-primary-200'
              : 'bg-stone-100 text-stone-400'
          ]"
        >
          <UIcon v-if="i === 0" name="i-heroicons-check" class="w-4 h-4" />
          <span v-else>{{ i + 1 }}</span>
        </div>
        <span
          :class="[
            'text-xs font-medium hidden sm:inline',
            i <= 1 ? 'text-primary-700' : 'text-stone-400'
          ]"
        >
          {{ step }}
        </span>
        <div v-if="i < steps.length - 1" class="w-6 h-px bg-stone-200" />
      </div>
    </div>

    <!-- Loading state (detecting token from URL) -->
    <div v-if="processing" class="py-8 space-y-4">
      <div class="w-16 h-16 mx-auto bg-primary-50 rounded-2xl flex items-center justify-center">
        <UIcon name="i-heroicons-arrow-path" class="w-8 h-8 text-primary-600 animate-spin" />
      </div>
      <p class="text-stone-600 font-medium">Confirmando sua conta...</p>
    </div>

    <!-- Success state -->
    <div v-else-if="confirmed" class="py-8 space-y-4">
      <div class="w-16 h-16 mx-auto bg-green-50 rounded-2xl flex items-center justify-center">
        <UIcon name="i-heroicons-check-circle" class="w-8 h-8 text-green-600" />
      </div>
      <h2 class="text-xl font-bold text-stone-900" style="font-family:var(--font-heading)">
        Email confirmado!
      </h2>
      <p class="text-stone-500 text-sm">Redirecionando para o app...</p>
    </div>

    <!-- Waiting for confirmation -->
    <div v-else class="py-4 space-y-6">
      <div class="w-20 h-20 mx-auto bg-primary-50 rounded-2xl flex items-center justify-center animate-mailbox-pulse">
        <UIcon name="i-heroicons-envelope" class="w-10 h-10 text-primary-600" />
      </div>

      <div>
        <h2 class="text-xl font-bold text-stone-900 mb-2" style="font-family:var(--font-heading)">
          Verifique seu email
        </h2>
        <p class="text-stone-500 text-sm leading-relaxed">
          Enviamos um link de confirmaÃ§Ã£o para
          <strong v-if="email" class="text-stone-700">{{ email }}</strong>.
          <br />Clique no link para ativar sua conta.
        </p>
      </div>

      <!-- Resend button -->
      <div class="space-y-3">
        <UButton
          :label="resendCooldown > 0 ? `Reenviar em ${resendCooldown}s` : 'Reenviar email de confirmaÃ§Ã£o'"
          :disabled="resendCooldown > 0 || resending"
          :loading="resending"
          block
          variant="outline"
          class="!rounded-xl"
          @click="resendEmail"
        />

        <p class="text-xs text-stone-400">
          Verifique tambÃ©m a pasta de spam. O link expira em 24 horas.
        </p>
      </div>

      <div class="pt-2 border-t border-stone-100">
        <NuxtLink to="/auth/login" class="text-sm text-primary-600 font-medium hover:underline">
          â† Voltar para o login
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore, type User } from '~/stores/auth'

definePageMeta({ layout: 'auth' })

const supabase = useSupabaseClient()
const { post } = useApi()
const authStore = useAuthStore()
const router = useRouter()
const toast = useToast()

const steps = ['Criar conta', 'Confirmar email', 'ComeÃ§ar']

const email = ref('')
const processing = ref(false)
const confirmed = ref(false)
const resending = ref(false)
const resendCooldown = ref(0)

let cooldownInterval: ReturnType<typeof setInterval> | null = null

type SessionResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

// Load email from sessionStorage
onMounted(() => {
  if (import.meta.client) {
    email.value = sessionStorage.getItem('confirm_email') || ''
  }

  // Check if Supabase passes tokens in the URL hash (after clicking confirmation link)
  handleHashTokens()
})

const handleHashTokens = async () => {
  if (!import.meta.client) return

  const hash = window.location.hash
  if (!hash || !hash.includes('access_token')) return

  processing.value = true

  try {
    // Supabase stores session automatically from the hash, just get it
    const { data, error } = await supabase.auth.getSession()

    if (error || !data.session) {
      throw new Error('NÃ£o foi possÃ­vel confirmar. Tente fazer login novamente.')
    }

    const response = await post<SessionResponse>('/auth/supabase/session', {
      accessToken: data.session.access_token,
      refreshToken: data.session.refresh_token,
    })

    authStore.setAuth(response)
    confirmed.value = true

    // Clean up
    sessionStorage.removeItem('confirm_email')
    window.history.replaceState(null, '', window.location.pathname)

    // Redirect after brief animation
    setTimeout(() => {
      router.push('/onboarding')
    }, 1500)
  } catch (error: unknown) {
    processing.value = false
    const message = error instanceof Error ? error.message : 'Erro ao confirmar email'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  }
}

const resendEmail = async () => {
  if (!email.value) {
    toast.add({
      title: 'Email nÃ£o encontrado',
      description: 'Volte para a tela de registro e cadastre-se novamente.',
      color: 'warning',
    })
    return
  }

  resending.value = true
  try {
    const { error } = await supabase.auth.resend({
      type: 'signup',
      email: email.value,
      options: {
        emailRedirectTo: `${window.location.origin}/auth/confirm`,
      },
    })

    if (error) throw new Error(error.message)

    toast.add({
      title: 'Email reenviado',
      description: 'Verifique sua caixa de entrada novamente.',
      color: 'success',
    })

    // Start cooldown
    resendCooldown.value = 60
    cooldownInterval = setInterval(() => {
      resendCooldown.value--
      if (resendCooldown.value <= 0 && cooldownInterval) {
        clearInterval(cooldownInterval)
        cooldownInterval = null
      }
    }, 1000)
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao reenviar'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    resending.value = false
  }
}

onUnmounted(() => {
  if (cooldownInterval) clearInterval(cooldownInterval)
})
</script>
