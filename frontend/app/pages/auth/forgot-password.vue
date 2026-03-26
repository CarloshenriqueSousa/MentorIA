<template>
  <div>
    <h2 class="text-2xl font-bold text-stone-900 mb-1" style="font-family:var(--font-heading)">
      Esqueceu a senha?
    </h2>
    <p class="text-stone-500 text-sm mb-8">
      Sem problemas. Informe seu email e enviaremos um link para redefinir sua senha.
    </p>

    <!-- Success state -->
    <div v-if="sent" class="text-center py-4 space-y-6">
      <div class="w-16 h-16 mx-auto bg-green-50 rounded-2xl flex items-center justify-center">
        <UIcon name="i-heroicons-paper-airplane" class="w-8 h-8 text-green-600" />
      </div>
      <div>
        <h3 class="text-lg font-semibold text-stone-900 mb-2">Email enviado!</h3>
        <p class="text-sm text-stone-500 leading-relaxed">
          Enviamos um link de redefiniÃ§Ã£o para
          <strong class="text-stone-700">{{ form.email }}</strong>.
          <br />Verifique sua caixa de entrada (e spam).
        </p>
      </div>
      <UButton
        label="Voltar para o login"
        to="/auth/login"
        block
        variant="outline"
        class="!rounded-xl"
      />
    </div>

    <!-- Form state -->
    <div v-else>
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

        <UButton
          type="submit"
          block
          size="lg"
          label="Enviar link de redefiniÃ§Ã£o"
          :loading="loading"
          class="!rounded-xl"
        />
      </UForm>

      <p class="text-center text-sm text-stone-500 mt-6">
        Lembrou a senha?
        <NuxtLink to="/auth/login" class="text-primary-600 font-semibold hover:underline">
          Voltar para o login
        </NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { z } from 'zod'

definePageMeta({ layout: 'auth' })

const supabase = useSupabaseClient()
const toast = useToast()

const loading = ref(false)
const sent = ref(false)

const schema = z.object({
  email: z.string().email('Email invÃ¡lido'),
})

const form = reactive({ email: '' })

const onSubmit = async () => {
  loading.value = true
  try {
    const { error } = await supabase.auth.resetPasswordForEmail(
      form.email.trim().toLowerCase(),
      {
        redirectTo: `${window.location.origin}/auth/reset-password`,
      },
    )

    if (error) throw new Error(error.message)

    sent.value = true
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao enviar email'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    loading.value = false
  }
}
</script>
