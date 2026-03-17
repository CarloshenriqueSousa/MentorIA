<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-4">
    <div class="text-center">
      <p class="text-8xl font-bold text-primary-600 mb-4">404</p>
      <h1 class="text-2xl font-bold text-slate-900 mb-2">Página não encontrada</h1>
      <p class="text-slate-500 mb-8">A página que você procura não existe ou foi movida.</p>
      <div class="flex gap-3 justify-center">
        <UButton label="Voltar" variant="outline" color="gray" @click="$router.back()" />
        <NuxtLink to="/dashboard">
          <UButton label="Ir para o Dashboard" />
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })
</script>