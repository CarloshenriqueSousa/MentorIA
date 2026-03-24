<template>
  <div>
    <h2 class="text-2xl font-bold text-slate-900 mb-1">Criar conta grátis</h2>
    <p class="text-slate-500 mb-8">Conta criada com Supabase Auth</p>

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

      <UButton
        type="submit"
        block
        size="lg"
        label="Criar conta"
        :loading="loading"
      />
    </UForm>

    <p class="text-center text-sm text-slate-600 mt-6">
      Já tem conta?
      <NuxtLink to="/auth/login" class="text-primary-600 font-medium hover:underline">
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

const schema = z.object({
  name: z.string().min(2, 'Nome deve ter pelo menos 2 caracteres'),
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'Senha deve ter pelo menos 8 caracteres'),
  confirmPassword: z.string(),
}).refine(data => data.password === data.confirmPassword, {
  message: 'Senhas não coincidem',
  path: ['confirmPassword'],
})

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
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
        data: {
          full_name: form.name.trim(),
        },
      },
    })

    if (error) {
      throw new Error(error.message)
    }

    if (!data.session) {
      toast.add({
        title: 'Confirme seu email',
        description: 'Enviamos um link de confirmação. Depois disso você poderá entrar.',
        color: 'info',
      })
      return
    }

    const response = await post<SessionResponse>('/auth/supabase/session', {
      accessToken: data.session.access_token,
      refreshToken: data.session.refresh_token,
    })

    authStore.setAuth(response)
    router.push('/onboarding')
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Tente novamente'
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
