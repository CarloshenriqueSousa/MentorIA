<template>
  <div>
    <h2 class="text-2xl font-bold text-stone-900 mb-1" style="font-family:var(--font-heading)">
      Bem-vindo de volta
    </h2>
    <p class="text-stone-500 text-sm mb-8">Entre na sua conta para continuar estudando</p>

    <UForm :schema="schema" :state="form" class="space-y-5" @submit="onSubmit">
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
          placeholder="Sua senha"
          icon="i-heroicons-lock-closed"
          size="lg"
          class="w-full"
          :trailing-icon="showPassword ? 'i-heroicons-eye-slash' : 'i-heroicons-eye'"
          @click:trailing="showPassword = !showPassword"
        />
      </UFormField>

      <div class="flex justify-end">
        <NuxtLink
          to="/auth/forgot-password"
          class="text-sm text-primary-600 font-medium hover:underline"
        >
          Esqueceu a senha?
        </NuxtLink>
      </div>

      <UButton
        type="submit"
        block
        size="lg"
        label="Entrar"
        :loading="loading"
        class="!rounded-xl"
      />
    </UForm>

    <!-- Divider -->
    <div class="relative my-8">
      <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-stone-200" /></div>
      <div class="relative flex justify-center text-xs">
        <span class="bg-white/85 px-3 text-stone-400">ou</span>
      </div>
    </div>

    <!-- Social login placeholders -->
    <div class="space-y-3">
      <button
        disabled
        class="w-full flex items-center justify-center gap-3 px-4 py-2.5 rounded-xl border border-stone-200 bg-white text-sm font-medium text-stone-500 cursor-not-allowed opacity-60"
      >
        <svg class="w-5 h-5" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
        Google (em breve)
      </button>
    </div>

    <p class="text-center text-sm text-stone-500 mt-8">
      NÃ£o tem conta?
      <NuxtLink to="/auth/register" class="text-primary-600 font-semibold hover:underline">
        Criar conta grÃ¡tis
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

const schema = z.object({
  email: z.string().email('Email invÃ¡lido'),
  password: z.string().min(6, 'Senha deve ter pelo menos 6 caracteres'),
})

const form = reactive({ email: '', password: '' })

type SessionResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

const onSubmit = async () => {
  loading.value = true
  try {
    const { data, error } = await supabase.auth.signInWithPassword({
      email: form.email.trim().toLowerCase(),
      password: form.password,
    })

    if (error) {
      // Supabase retorna "Invalid login credentials" â€” traduzir
      if (error.message.includes('Invalid login credentials')) {
        throw new Error('Email ou senha incorretos. Verifique seus dados e tente novamente.')
      }
      if (error.message.includes('Email not confirmed')) {
        throw new Error('Email ainda nÃ£o confirmado. Verifique sua caixa de entrada.')
      }
      throw new Error(error.message)
    }
    if (!data.session) {
      throw new Error('SessÃ£o nÃ£o retornada. Confirme seu email para poder entrar.')
    }

    const response = await post<SessionResponse>('/auth/supabase/session', {
      accessToken: data.session.access_token,
      refreshToken: data.session.refresh_token,
    })

    authStore.setAuth(response)

    if (!response.user.completedOnboarding) {
      router.push('/onboarding')
    } else {
      router.push('/dashboard')
    }
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao entrar. Tente novamente.'
    toast.add({
      title: 'Erro ao entrar',
      description: message,
      color: 'error',
    })
  } finally {
    loading.value = false
  }
}
</script>
