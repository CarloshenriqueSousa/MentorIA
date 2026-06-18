<template>
  <div>
    <h2 class="text-2xl font-bold text-stone-900 mb-1">Criar conta grátis</h2>
    <p class="text-stone-500 text-sm mb-8">Comece a estudar com Inteligência Artificial</p>

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
          placeholder="Mínimo 8 caracteres"
          icon="i-heroicons-lock-closed"
          size="lg"
          class="w-full"
          :trailing-icon="showPassword ? 'i-heroicons-eye-slash' : 'i-heroicons-eye'"
          @click:trailing="showPassword = !showPassword"
        />
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
          <NuxtLink to="/privacy" class="text-primary-600 hover:underline" target="_blank">Política de Privacidade</NuxtLink>.
        </span>
      </label>

      <UButton
        type="submit"
        block
        size="lg"
        label="Criar conta"
        :loading="loading"
        :disabled="!form.acceptTerms"
      />
    </UForm>

    <p class="text-center text-sm text-stone-500 mt-6">
      Já tem conta?
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
const { post } = useApi()
const toast = useToast()

const loading = ref(false)
const showPassword = ref(false)

const schema = z.object({
  name: z.string().min(2, 'Nome deve ter pelo menos 2 caracteres'),
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'Senha deve ter pelo menos 8 caracteres'),
  confirmPassword: z.string(),
  acceptTerms: z.literal(true, { errorMap: () => ({ message: 'Aceite os termos para continuar' }) }),
}).refine(data => data.password === data.confirmPassword, {
  message: 'Senhas não coincidem',
  path: ['confirmPassword'],
})

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false,
})

type SessionResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

const onSubmit = async () => {
  loading.value = true
  try {
    const response = await post<SessionResponse>('/auth/register', {
      name: form.name.trim(),
      email: form.email.trim().toLowerCase(),
      password: form.password,
    })

    authStore.setAuth(response)
    await router.push('/onboarding')
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
