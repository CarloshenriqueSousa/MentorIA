<template>
  <div>
    <h2 class="text-2xl font-bold text-slate-900 mb-1">Bem-vindo de volta</h2>
    <p class="text-slate-500 mb-8">Entre com email e senha (Supabase Auth)</p>

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
        <UButton variant="link" color="primary" size="sm" label="Esqueceu a senha?" />
      </div>

      <UButton
        type="submit"
        block
        size="lg"
        label="Entrar"
        :loading="loading"
      />

      <div class="relative my-6">
        <div class="absolute inset-0 flex items-center">
          <span class="w-full border-t border-slate-200" />
        </div>
        <div class="relative flex justify-center text-xs uppercase">
          <span class="bg-white px-2 text-slate-400">Ou continue com</span>
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
      />
    </UForm>

    <UCard class="mt-10" :ui="{ body: 'p-4 sm:p-5' }">
      <template #header>
        <h3 class="text-sm font-semibold text-slate-800">
          Conta local (desenvolvimento / teste)
        </h3>
      </template>
      <p class="text-xs text-slate-500 mb-4">
        Backend Spring (<code class="text-primary-600">POST /auth/login</code>). Conta padrão:
        <strong>teste@gmail.com</strong> / <strong>teste123</strong> (seed no backend).
      </p>
      <UForm :schema="localSchema" :state="localForm" class="space-y-4" @submit="onSubmitLocal">
        <UFormField label="Email" name="email">
          <UInput
            v-model="localForm.email"
            type="email"
            placeholder="teste@gmail.com"
            icon="i-heroicons-envelope"
            size="md"
            class="w-full"
          />
        </UFormField>
        <UFormField label="Senha" name="password">
          <UInput
            v-model="localForm.password"
            type="password"
            placeholder="teste123"
            icon="i-heroicons-lock-closed"
            size="md"
            class="w-full"
          />
        </UFormField>
        <UButton
          type="submit"
          block
          label="Entrar com conta local"
          color="neutral"
          :loading="loadingLocal"
        />
      </UForm>
    </UCard>

    <p class="text-center text-sm text-slate-600 mt-6">
      Não tem conta?
      <NuxtLink to="/auth/register" class="text-primary-600 font-medium hover:underline">
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
const supabase = useSupabaseClient()
const { post } = useApi()
const config = useRuntimeConfig()

const loading = ref(false)
const loadingLocal = ref(false)
const showPassword = ref(false)

const schema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'Senha deve ter pelo menos 8 caracteres'),
})

const localSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Senha deve ter pelo menos 6 caracteres'),
})

const form = reactive({
  email: '',
  password: '',
})

const localForm = reactive({
  email: 'teste@gmail.com',
  password: '',
})

const toast = useToast()

type SessionResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

const onSubmit = async () => {
  loading.value = true
  try {
    const supabaseUrl = String(config.public.supabaseUrl || '').trim()
    const supabaseKey = String(config.public.supabaseKey || '').trim()
    const supabaseConfigured =
      supabaseUrl.length > 0
      && supabaseKey.length > 0
      && !supabaseUrl.includes('SEU_REF')
      && !supabaseKey.includes('SUA_CHAVE')

    if (!supabaseConfigured) {
      throw new Error('Supabase não configurado. Preencha SUPABASE_URL e SUPABASE_KEY no .env/.env do frontend.')
    }

    const { data, error } = await supabase.auth.signInWithPassword({
      email: form.email.trim().toLowerCase(),
      password: form.password,
    })

    if (error) {
      throw new Error(error.message)
    }
    if (!data.session) {
      throw new Error('Sessão não retornada. Confirme seu email se o projeto exigir verificação.')
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

const onSubmitLocal = async () => {
  loadingLocal.value = true
  try {
    const response = await post<SessionResponse>('/auth/login', {
      email: localForm.email.trim().toLowerCase(),
      password: localForm.password,
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
    const message = error instanceof Error ? error.message : 'Falha no login local'
    toast.add({
      title: 'Login local',
      description: message,
      color: 'error',
    })
  } finally {
    loadingLocal.value = false
  }
}
</script>
