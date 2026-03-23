<template>
  <div class="min-h-screen bg-white">

    <!-- Navbar -->
    <header class="sticky top-0 z-50 bg-white/80 backdrop-blur border-b border-slate-200">
      <div class="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-5 h-5" />
          </div>
          <span class="font-bold text-slate-900 text-xl">MentorIA</span>
        </div>
        <div class="flex items-center gap-3">
          <NuxtLink to="/auth/login">
            <UButton variant="ghost" color="neutral" label="Entrar" />
          </NuxtLink>
          <NuxtLink to="/auth/register">
            <UButton label="Começar grátis" />
          </NuxtLink>
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section class="max-w-7xl mx-auto px-4 py-24 text-center">
      <UBadge label="🚀 Novo: Planos de estudo com IA" color="primary" variant="subtle" class="mb-6" />
      <h1 class="text-5xl font-bold text-slate-900 mb-6 leading-tight">
        Seu mentor de estudos<br >
        <span class="text-primary-600">disponível 24 horas</span>
      </h1>
      <p class="text-xl text-slate-600 mb-10 max-w-2xl mx-auto">
        Prepare-se para concursos públicos com um mentor de IA personalizado. Planos de estudo adaptativos, chat interativo e acompanhamento de progresso.
      </p>
      <div class="flex items-center justify-center gap-4 flex-wrap">
        <NuxtLink to="/auth/register">
          <UButton size="xl" label="Começar grátis agora" trailing-icon="i-heroicons-arrow-right" />
        </NuxtLink>
        <UButton size="xl" variant="outline" color="neutral" label="Ver como funciona" />
      </div>
      <p class="text-sm text-slate-400 mt-4">Grátis para começar • Sem cartão de crédito</p>
    </section>

    <!-- Features -->
    <section class="bg-slate-50 py-24">
      <div class="max-w-7xl mx-auto px-4">
        <h2 class="text-3xl font-bold text-center text-slate-900 mb-16">
          Tudo que você precisa para passar
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <UCard v-for="feature in features" :key="feature.title" class="text-center">
            <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center mx-auto mb-4">
              <UIcon :name="feature.icon" class="text-primary-600 w-6 h-6" />
            </div>
            <h3 class="font-semibold text-slate-900 mb-2">{{ feature.title }}</h3>
            <p class="text-slate-500 text-sm">{{ feature.description }}</p>
          </UCard>
        </div>
      </div>
    </section>

    <!-- Pricing -->
    <section class="py-24">
      <div class="max-w-7xl mx-auto px-4">
        <h2 class="text-3xl font-bold text-center text-slate-900 mb-4">Planos simples e acessíveis</h2>
        <p class="text-center text-slate-500 mb-16">Comece grátis e faça upgrade quando quiser</p>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          <UCard
v-for="plan in plans" :key="plan.name"
            :class="plan.featured ? 'ring-2 ring-primary-600' : ''">
            <div v-if="plan.featured" class="text-center mb-4">
              <UBadge label="Mais popular" color="primary" />
            </div>
            <h3 class="font-bold text-xl text-slate-900 mb-1">{{ plan.name }}</h3>
            <div class="flex items-end gap-1 mb-6">
              <span class="text-4xl font-bold text-slate-900">{{ plan.price }}</span>
              <span class="text-slate-500 mb-1">/mês</span>
            </div>
            <ul class="space-y-3 mb-8">
              <li v-for="item in plan.features" :key="item" class="flex items-center gap-2 text-sm text-slate-600">
                <UIcon name="i-heroicons-check" class="text-primary-600 w-4 h-4 flex-shrink-0" />
                {{ item }}
              </li>
            </ul>
            <NuxtLink to="/auth/register">
              <UButton
                :variant="plan.featured ? 'solid' : 'outline'"
                :color="plan.featured ? 'primary' : 'neutral'"
                block
                :label="plan.cta"
              />
            </NuxtLink>
          </UCard>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="border-t border-slate-200 py-12">
      <div class="max-w-7xl mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-4">
        <div class="flex items-center gap-2">
          <div class="w-6 h-6 bg-primary-600 rounded flex items-center justify-center">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-4 h-4" />
          </div>
          <span class="font-semibold text-slate-900">MentorIA</span>
        </div>
        <div class="flex items-center gap-6 text-sm text-slate-500">
          <NuxtLink to="/terms" class="hover:text-slate-900">Termos de uso</NuxtLink>
          <NuxtLink to="/privacy" class="hover:text-slate-900">Privacidade</NuxtLink>
        </div>
        <p class="text-sm text-slate-400">© 2025 MentorIA. Todos os direitos reservados.</p>
      </div>
    </footer>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

const features = [
  {
    icon: 'i-heroicons-chat-bubble-left-right',
    title: 'Chat com Mentor IA',
    description: 'Tire dúvidas a qualquer hora com um mentor personalizado para o seu concurso.',
  },
  {
    icon: 'i-heroicons-book-open',
    title: 'Plano de Estudos',
    description: 'Receba um cronograma adaptado ao seu nível, disponibilidade e objetivo.',
  },
  {
    icon: 'i-heroicons-chart-bar',
    title: 'Acompanhamento',
    description: 'Visualize seu progresso, mantenha a sequência de estudos e ganhe XP.',
  },
]

const plans = [
  {
    name: 'Grátis',
    price: 'R$0',
    featured: false,
    cta: 'Começar grátis',
    features: [
      '10 mensagens por dia',
      '2 planos de estudo',
      'Dashboard básico',
      'Suporte por email',
    ],
  },
  {
    name: 'Básico',
    price: 'R$29',
    featured: true,
    cta: 'Assinar agora',
    features: [
      'Mensagens ilimitadas',
      'Planos ilimitados',
      'Dashboard completo',
      'Histórico completo',
      'Suporte prioritário',
    ],
  },
  {
    name: 'Premium',
    price: 'R$49',
    featured: false,
    cta: 'Assinar Premium',
    features: [
      'Tudo do Básico',
      'Gamificação completa',
      'Relatórios avançados',
      'Acesso antecipado',
      'Suporte VIP',
    ],
  },
]
</script>