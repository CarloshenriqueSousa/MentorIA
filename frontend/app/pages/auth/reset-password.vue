<template>
  <div>
    <h2 class="text-2xl font-bold text-stone-900 mb-1" style="font-family:var(--font-heading)">
      Redefinir senha
    </h2>
    <p class="text-stone-500 text-sm mb-8">Escolha uma nova senha segura para sua conta.</p>

    <UForm :schema="schema" :state="form" class="space-y-5" @submit="onSubmit">
      <UFormField label="Nova senha" name="password">
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

      <UFormField label="Confirmar nova senha" name="confirmPassword">
        <UInput
          v-model="form.confirmPassword"
          :type="showPassword ? 'text' : 'password'"
          placeholder="Repita a nova senha"
          icon="i-heroicons-lock-closed"
          size="lg"
          class="w-full"
        />
      </UFormField>

      <UButton
        type="submit"
        block
        size="lg"
        label="Redefinir senha"
        :loading="loading"
        class="!rounded-xl"
      />
    </UForm>

    <p class="text-center text-sm text-stone-500 mt-6">
      <NuxtLink to="/auth/login" class="text-primary-600 font-semibold hover:underline">
        â† Voltar para o login
      </NuxtLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { z } from 'zod'

definePageMeta({ layout: 'auth' })

const supabase = useSupabaseClient()
const router = useRouter()
const toast = useToast()

const loading = ref(false)
const showPassword = ref(false)

const schema = z.object({
  password: z.string().min(8, 'Senha deve ter pelo menos 8 caracteres'),
  confirmPassword: z.string(),
}).refine(data => data.password === data.confirmPassword, {
  message: 'Senhas nÃ£o coincidem',
  path: ['confirmPassword'],
})

const form = reactive({ password: '', confirmPassword: '' })

// Password strength
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

const onSubmit = async () => {
  loading.value = true
  try {
    const { error } = await supabase.auth.updateUser({
      password: form.password,
    })

    if (error) throw new Error(error.message)

    toast.add({
      title: 'Senha redefinida!',
      description: 'Sua senha foi alterada com sucesso. FaÃ§a login com a nova senha.',
      color: 'success',
    })

    // Sign out to force a fresh login with the new password
    await supabase.auth.signOut()

    setTimeout(() => {
      router.push('/auth/login')
    }, 1500)
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao redefinir senha'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    loading.value = false
  }
}
</script>
