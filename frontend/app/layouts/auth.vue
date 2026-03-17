<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <NuxtLink to="/" class="inline-flex items-center gap-3">
          <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-6 h-6" />
          </div>
          <span class="font-bold text-slate-900 text-2xl">MentorIA</span>
        </NuxtLink>
      </div>

      <!-- Card -->
      <UCard class="shadow-lg">
        <slot />
      </UCard>

      <!-- Footer -->
      <p class="text-center text-sm text-slate-500 mt-6">
        {{ $t('auth.terms_notice') }}
        <NuxtLink to="/terms" class="text-primary-600 hover:underline">{{ $t('auth.terms') }}</NuxtLink>
        e
        <NuxtLink to="/privacy" class="text-primary-600 hover:underline">{{ $t('auth.privacy') }}</NuxtLink>
      </p>
    </div>
  </div>
</template>