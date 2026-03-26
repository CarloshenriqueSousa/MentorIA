<template>
  <div>
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
            i === 0
              ? 'bg-primary-600 text-white shadow-md shadow-primary-200'
              : 'bg-stone-100 text-stone-400'
          ]"
        >
          {{ i + 1 }}
        </div>
        <span
          :class="[
            'text-xs font-medium hidden sm:inline',
            i === 0 ? 'text-primary-700' : 'text-stone-400'
          ]"
        >
          {{ step }}
        </span>
        <div v-if="i < steps.length - 1" class="w-6 h-px bg-stone-200" />
      </div>
    </div>

    <h2 class="text-2xl font-bold text-stone-900 mb-1" style="font-family:var(--font-heading)">
      Criar conta grÃ¡tis
    </h2>
    <p class="text-stone-500 text-sm mb-8">Comece a estudar com InteligÃªncia Artificial</p>

    <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
      <UFormField label="Nome completo" name="name">
        <UInput
          v-model="form.name"
          placeholder="Seu nome"
          icon="i-heroicons-user"
          size="lg"
          class="w-full"
        />
      </UFormField>

      <UFormField label="Email" name="email">
        <UInput
          v-model="form.email"
          type="email"
          placeholder="seu@email.com"
          icon="i-heroicons-envelope"
          size="lg"
          class="w-full"
        />
      </UFormField>

      <UFormField label="Senha" name="password">
        <UInput
          v-model="form.password"
          :type="showPassword ? 'text' : 'password'"
          placeholder="MÃ­nimo 8 caracteres"
          icon="i-heroicons-lock-closed"
          size="lg"
          class="w-full"
          :trailing-icon="showPassword ? 'i-heroicons-eye-slash' : 'i-heroicons-eye'"
          @click:trailing="showPassword = !showPassword"
        />
        <!-- Password strength bar -->
        <div class="mt-2">
          <div class="w-full bg-stone-100 rounded-full h-1 overflow-hidden">
            <div :class="['strength-bar', `strength-${passwordStrength}`]" />
          </div>
          <p :class="['text-xs mt-1', strengthColor]">{{ strengthLabel }}</p>
        </div>
      </UFormField>

      <UFormField label="Confirmar senha" name="confirmPassword">
        <UInput
          v-model="form.confirmPassword"
          :type="showPassword ? 'text' : 'password'"
          placeholder="Repita a senha"
          icon="i-heroicons-lock-closed"
          size="lg"
          class="w-full"
        />
      </UFormField>

      <!-- Terms checkbox -->
      <label class="flex items-start gap-3 cursor-pointer select-none">
        <input
          v-model="form.acceptTerms"
          type="checkbox"
          class="mt-0.5 h-4 w-4 rounded border-stone-300 text-primary-600 focus:ring-primary-500"
        />
        <span class="text-xs text-stone-500 leading-relaxed">
          Li e aceito os
          <NuxtLink to="/terms" class="text-primary-600 hover:underline" target="_blank">Termos de Uso</NuxtLink>
          e a
          <NuxtLink to="/privacy" class="text-primary-600 hover:underline" target="_blank">PolÃ­tica de Privacidade</NuxtLink>.
        </span>
      </label>

      <UButton
        type="submit"
        block
        size="lg"
        label="Criar conta"
        :loading="loading"
        :disabled="!form.acceptTerms"
        class="!rounded-xl"
      />

      <div class="relative my-6">
        <div class="absolute inset-0 flex items-center">
          <span class="w-full border-t border-stone-200" />
        </div>
        <div class="relative flex justify-center text-xs uppercase">
          <span class="bg-white px-2 text-stone-400">Ou crie com</span>
        </div>
      </div>

      <UButton
        color="neutral"
        variant="outline"
        block
        disabled
        size="lg"
        icon="i-simple-icons-google"
        label="Google (em breve)"
        class="!rounded-xl"
      />
    </UForm>

    <p class="text-center text-sm text-stone-500 mt-6">
      JÃ¡ tem conta?
      <NuxtLink to="/auth/login" class="text-primary-600 font-semibold hover:underline">
        Entrar
      </NuxtLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { z } from 'zod'
import { useAuthStore, type User } from '~/stores/auth'

definePageMeta({ layout: 'auth' })

const authStore = useAuthStore()
const router = useRouter()
const supabase = useSupabaseClient()
const { post } = useApi()
const toast = useToast()

const loading = ref(false)
const showPassword = ref(false)

const steps = ['Criar conta', 'Confirmar email', 'ComeÃ§ar']

const schema = z.object({
  name: z.string().min(2, 'Nome deve ter pelo menos 2 caracteres'),
  email: z.string().email('Email invÃ¡lido'),
  password: z.string().min(8, 'Senha deve ter pelo menos 8 caracteres'),
  confirmPassword: z.string(),
  acceptTerms: z.literal(true, { errorMap: () => ({ message: 'Aceite os termos para continuar' }) }),
}).refine(data => data.password === data.confirmPassword, {
  message: 'Senhas nÃ£o coincidem',
  path: ['confirmPassword'],
})

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false,
})

// â”€â”€ Password strength â”€â”€
const passwordStrength = computed(() => {
  const p = form.password
  if (!p) return 0
  let score = 0
  if (p.length >= 8) score++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) score++
  if (/\d/.test(p)) score++
  if (/[^a-zA-Z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => {
  const labels = ['', 'Fraca', 'RazoÃ¡vel', 'Boa', 'Forte']
  return labels[passwordStrength.value] || ''
})

const strengthColor = computed(() => {
  const colors = ['text-stone-400', 'text-red-500', 'text-orange-500', 'text-yellow-600', 'text-green-600']
  return colors[passwordStrength.value] || 'text-stone-400'
})

type SessionResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

const onSubmit = async () => {
  loading.value = true
  try {
    const { data, error } = await supabase.auth.signUp({
      email: form.email.trim().toLowerCase(),
      password: form.password,
      options: {
        data: { full_name: form.name.trim() },
        emailRedirectTo: `${window.location.origin}/auth/confirm`,
      },
    })

    if (error) {
      throw new Error(error.message)
    }

    // Supabase returns no session when email confirmation is required
    if (!data.session) {
      // Store email for the confirm page
      if (import.meta.client) {
        sessionStorage.setItem('confirm_email', form.email.trim().toLowerCase())
      }
      router.push('/auth/confirm')
      return
    }

    // If Supabase auto-confirms (no email verification configured)
    const response = await post<SessionResponse>('/auth/supabase/session', {
      accessToken: data.session.access_token,
      refreshToken: data.session.refresh_token,
    })

    authStore.setAuth(response)
    router.push('/onboarding')
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao criar conta. Tente novamente.'
    toast.add({
      title: 'Erro ao criar conta',
      description: message,
      color: 'error',
    })
  } finally {
    loading.value = false
  }
}
</script>
