<template>
  <div class="text-center py-4 space-y-6">
    <div class="w-16 h-16 mx-auto bg-primary-50 rounded-2xl flex items-center justify-center">
      <UIcon name="i-heroicons-lock-closed" class="w-8 h-8 text-primary-600" />
    </div>
    <div>
      <h2 class="text-xl font-bold text-stone-900 mb-2">Redefinição de senha</h2>
      <p class="text-sm text-stone-500 leading-relaxed">
        A redefinição por email ainda não está disponível.
        Se você já tem uma conta, altere a senha em
        <NuxtLink to="/profile" class="text-primary-600 hover:underline">Meu Perfil</NuxtLink>.
      </p>
    </div>
    <UButton label="Voltar para o login" to="/auth/login" block variant="outline" />
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'auth' })
</script>
