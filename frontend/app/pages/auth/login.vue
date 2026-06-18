<template>
  <div>
    <h2 class="text-2xl font-bold text-slate-900 mb-1">Bem-vindo de volta</h2>
    <p class="text-slate-500 mb-8">Entre com email e senha</p>

    <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
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
        <NuxtLink to="/auth/forgot-password" class="text-sm text-primary-600 hover:underline">
          Esqueceu a senha?
        </NuxtLink>
      </div>

      <UButton
        type="submit"
        block
        size="lg"
        label="Entrar"
        :loading="loading"
      />
    </UForm>

    <p class="text-center text-sm text-stone-500 mt-8">
      Não tem conta?
      <NuxtLink to="/auth/register" class="text-primary-600 font-semibold hover:underline">
        Criar conta grátis
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
const { post } = useApi()
const toast = useToast()

const loading = ref(false)
const showPassword = ref(false)

const schema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Senha deve ter pelo menos 6 caracteres'),
})

const form = reactive({
  email: '',
  password: '',
})

type SessionResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

const onSubmit = async () => {
  loading.value = true
  try {
    const response = await post<SessionResponse>('/auth/login', {
      email: form.email.trim().toLowerCase(),
      password: form.password,
    })

    authStore.setAuth(response)

    if (response.user.role === 'ADMIN') {
      await router.push('/admin')
    } else if (!response.user.completedOnboarding) {
      await router.push('/onboarding')
    } else {
      await router.push('/dashboard')
    }
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Email ou senha incorretos'
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
