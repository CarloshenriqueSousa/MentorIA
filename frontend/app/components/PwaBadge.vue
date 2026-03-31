<template>
  <ClientOnly>
    <div
      v-if="$pwa?.offlineReady || $pwa?.needRefresh"
      class="fixed bottom-4 right-4 z-50 rounded-xl bg-white p-4 shadow-2xl border border-slate-200 max-w-sm w-full animate-fade-in-up"
    >
      <div class="flex items-start gap-4">
        <div class="p-2 bg-primary-50 rounded-lg text-primary-600">
          <UIcon name="i-heroicons-arrow-path-rounded-square" class="w-5 h-5" />
        </div>
        <div class="flex-1">
          <h3 class="font-semibold text-slate-900 text-sm">Atualização disponível</h3>
          <p class="text-xs text-slate-500 mt-1 mb-3">
            <span v-if="$pwa.offlineReady">App pronto para uso offline!</span>
            <span v-else>Novo conteúdo disponível, recarregue para atualizar.</span>
          </p>
          <div class="flex gap-2">
            <UButton
              v-if="$pwa.needRefresh"
              size="xs"
              label="Atualizar agora"
              @click="$pwa.updateServiceWorker()"
            />
            <UButton
              size="xs"
              variant="ghost"
              color="neutral"
              label="Fechar"
              @click="$pwa.cancelPrompt()"
            />
          </div>
        </div>
      </div>
    </div>
  </ClientOnly>
</template>

<style scoped>
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  animation: fade-in-up 0.3s ease-out forwards;
}
</style>
