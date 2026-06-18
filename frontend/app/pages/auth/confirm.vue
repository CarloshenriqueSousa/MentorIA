<template>
  <div class="text-center py-4 space-y-6">
    <div class="w-16 h-16 mx-auto bg-green-50 rounded-2xl flex items-center justify-center">
      <UIcon name="i-heroicons-check-circle" class="w-8 h-8 text-green-600" />
    </div>
    <div>
      <h2 class="text-xl font-bold text-stone-900 mb-2">Conta criada!</h2>
      <p class="text-sm text-stone-500">
        Sua conta está pronta. Faça login para começar a estudar.
      </p>
    </div>
    <UButton label="Ir para o login" to="/auth/login" block />
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'auth' })
</script>
