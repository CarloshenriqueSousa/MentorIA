<template>
  <div class="min-h-screen flex">
    <!-- Left panel — warm gradient hero (hidden on mobile) -->
    <div class="hidden lg:flex lg:w-1/2 auth-gradient-bg relative overflow-hidden items-center justify-center p-12">
      <!-- Floating dots -->
      <div
        v-for="dot in dots"
        :key="dot.id"
        class="floating-dot"
        :style="{
          width: dot.size + 'px',
          height: dot.size + 'px',
          left: dot.left + '%',
          bottom: '-' + dot.size + 'px',
          animationDuration: dot.duration + 's',
          animationDelay: dot.delay + 's',
        }"
      />

      <!-- Hero content -->
      <div class="relative z-10 text-center text-white max-w-md animate-fade-in-up">
        <div class="w-20 h-20 mx-auto mb-8 bg-white/15 backdrop-blur rounded-2xl flex items-center justify-center">
          <UIcon name="i-heroicons-academic-cap" class="w-10 h-10 text-white" />
        </div>
        <h1 class="text-4xl font-bold mb-4" style="font-family:var(--font-heading)">MentorIA</h1>
        <p class="text-lg text-white/85 leading-relaxed">
          Seu mentor de estudos com Inteligência Artificial. Planos personalizados, materiais curados e agentes inteligentes para acelerar seu aprendizado.
        </p>
        <div class="mt-10 flex items-center justify-center gap-6 text-sm text-white/65">
          <span class="flex items-center gap-1.5">
            <UIcon name="i-heroicons-shield-check" class="w-4 h-4" /> Seguro
          </span>
          <span class="flex items-center gap-1.5">
            <UIcon name="i-heroicons-bolt" class="w-4 h-4" /> Inteligente
          </span>
          <span class="flex items-center gap-1.5">
            <UIcon name="i-heroicons-sparkles" class="w-4 h-4" /> Personalizado
          </span>
        </div>
      </div>
    </div>

    <!-- Right panel — form -->
    <div class="flex-1 flex flex-col items-center justify-center p-6 sm:p-10 bg-gradient-to-br from-orange-50/40 to-stone-100/60">
      <!-- Mobile logo (shown only on small screens) -->
      <div class="lg:hidden mb-8 text-center animate-fade-in-up">
        <NuxtLink to="/" class="inline-flex items-center gap-3">
          <div class="w-10 h-10 bg-orange-500 rounded-xl flex items-center justify-center">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-6 h-6" />
          </div>
          <span class="font-bold text-stone-900 text-2xl" style="font-family:var(--font-heading)">MentorIA</span>
        </NuxtLink>
      </div>

      <div class="w-full max-w-md animate-fade-in-up-delay">
        <!-- Card -->
        <div class="glass-card rounded-2xl p-8">
          <slot />
        </div>

        <!-- Footer -->
        <p class="text-center text-xs text-stone-400 mt-6">
          Ao continuar, você concorda com nossos
          <NuxtLink to="/terms" class="text-orange-600 hover:underline">Termos de Uso</NuxtLink>
          e
          <NuxtLink to="/privacy" class="text-orange-600 hover:underline">Política de Privacidade</NuxtLink>.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Floating dots random generation
const dots = Array.from({ length: 18 }, (_, i) => ({
  id: i,
  size: Math.random() * 16 + 6,
  left: Math.random() * 100,
  duration: Math.random() * 12 + 10,
  delay: Math.random() * 8,
}))
</script>