<template>
  <div>
    <h2 class="text-2xl font-bold text-slate-900 mb-1">Bem-vindo de volta</h2>
    <p class="text-slate-500 mb-8">Entre na sua conta para continuar estudando</p>

    <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
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
    </UForm>

    <UDivider label="ou" class="my-6" />

    <p class="text-center text-sm text-slate-600">
      Não tem conta?
      <NuxtLink to="/auth/register" class="text-primary-600 font-medium hover:underline">
        Criar conta grátis
      </NuxtLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { z } from 'zod'
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: 'auth' })

const authStore = useAuthStore()
const router = useRouter()
const { post } = useApi()

const loading = ref(false)
const showPassword = ref(false)

const schema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'Senha deve ter pelo menos 8 caracteres'),
})

const form = reactive({
  email: '',
  password: '',
})

const toast = useToast()

const onSubmit = async () => {
  loading.value = true
  try {
    const response = await post<any>('/auth/login', form)
    authStore.setAuth(response)

    if (!response.user.completedOnboarding) {
      router.push('/onboarding')
    } else {
      router.push('/dashboard')
    }
  } catch (error: any) {
    toast.add({
      title: 'Erro ao entrar',
      description: error.message || 'Email ou senha incorretos',
      color: 'red',
    })
  } finally {
    loading.value = false
  }
}
</script>