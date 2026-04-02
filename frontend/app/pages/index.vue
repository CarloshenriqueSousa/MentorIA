<template>
  <div class="min-h-screen overflow-hidden" style="background: linear-gradient(135deg, #faf8f5 0%, #f0eeff 50%, #faf8f5 100%)">

    <!-- Navbar -->
    <header class="sticky top-0 z-50 backdrop-blur-xl border-b border-slate-200/60" style="background: rgba(255,255,255,0.75)">
      <div class="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 bg-gradient-to-br from-primary-500 to-primary-700 rounded-xl flex items-center justify-center shadow-lg shadow-primary-500/25">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-5 h-5" />
          </div>
          <span class="font-bold text-slate-900 text-xl tracking-tight" style="font-family: 'Outfit', sans-serif">MentorIA</span>
        </div>
        <div class="flex items-center gap-3">
          <NuxtLink to="/auth/login">
            <UButton variant="ghost" color="neutral" label="Entrar" />
          </NuxtLink>
          <NuxtLink to="/auth/register">
            <UButton label="Começar grátis" class="shadow-lg shadow-primary-500/25" />
          </NuxtLink>
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section class="relative max-w-7xl mx-auto px-4 pt-20 pb-28 text-center">
      <!-- Decorative blobs -->
      <div class="absolute -top-24 -left-24 w-96 h-96 bg-primary-300/20 rounded-full blur-3xl animate-blob" />
      <div class="absolute -top-12 -right-24 w-80 h-80 bg-violet-300/20 rounded-full blur-3xl animate-blob" style="animation-delay: 2s" />
      <div class="absolute top-48 left-1/2 w-72 h-72 bg-pink-200/15 rounded-full blur-3xl animate-blob" style="animation-delay: 4s" />

      <div class="relative z-10">
        <div class="animate-fade-in">
          <UBadge label="🚀 Novo: Planos de estudo com IA" color="primary" variant="subtle" class="mb-6 text-sm" />
        </div>

        <h1 class="text-5xl md:text-6xl lg:text-7xl font-extrabold text-slate-900 mb-6 leading-[1.1] tracking-tight animate-fade-in-up" style="font-family: 'Outfit', sans-serif">
          Seu mentor de estudos<br>
          <span class="bg-gradient-to-r from-primary-600 via-violet-600 to-primary-600 bg-clip-text text-transparent bg-[length:200%_auto] animate-gradient">disponível 24 horas</span>
        </h1>

        <p class="text-xl text-slate-600 mb-10 max-w-2xl mx-auto animate-fade-in-up" style="animation-delay: 0.15s">
          Prepare-se para concursos públicos com um mentor de IA personalizado.
          Planos de estudo adaptativos, chat interativo e acompanhamento de progresso.
        </p>

        <div class="flex items-center justify-center gap-4 flex-wrap animate-fade-in-up" style="animation-delay: 0.3s">
          <NuxtLink to="/auth/register">
            <UButton size="xl" label="Começar grátis agora" trailing-icon="i-heroicons-arrow-right" class="shadow-xl shadow-primary-500/30 hover:shadow-primary-500/40 transition-shadow" />
          </NuxtLink>
          <UButton size="xl" variant="outline" color="neutral" label="Ver como funciona" @click="scrollToFeatures" />
        </div>

        <p class="text-sm text-slate-400 mt-4 animate-fade-in-up" style="animation-delay: 0.4s">
          Grátis para começar • Sem cartão de crédito
        </p>
      </div>

      <!-- Stats counters -->
      <div class="relative z-10 grid grid-cols-3 gap-6 max-w-lg mx-auto mt-16 animate-fade-in-up" style="animation-delay: 0.5s">
        <div v-for="stat in heroStats" :key="stat.label" class="text-center">
          <p class="text-3xl font-bold text-slate-900" style="font-family: 'Outfit', sans-serif">{{ stat.value }}</p>
          <p class="text-xs text-slate-500 mt-1">{{ stat.label }}</p>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section id="features" class="py-24" style="background: rgba(255,255,255,0.5)">
      <div class="max-w-7xl mx-auto px-4">
        <div class="text-center mb-16">
          <UBadge label="Funcionalidades" color="primary" variant="subtle" class="mb-4" />
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 tracking-tight" style="font-family: 'Outfit', sans-serif">
            Tudo que você precisa para passar
          </h2>
          <p class="text-slate-500 mt-3 max-w-xl mx-auto">
            Ferramentas inteligentes que se adaptam ao seu ritmo e estilo de aprendizagem
          </p>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            v-for="(feature, i) in features"
            :key="feature.title"
            class="group relative rounded-2xl border border-slate-200/80 bg-white/70 backdrop-blur-sm p-8 transition-all duration-300 hover:shadow-xl hover:shadow-primary-500/10 hover:-translate-y-1"
            :style="`animation-delay: ${i * 0.1}s`"
          >
            <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-5 transition-transform duration-300 group-hover:scale-110" :class="feature.bgClass">
              <UIcon :name="feature.icon" :class="feature.iconClass" class="w-7 h-7" />
            </div>
            <h3 class="font-bold text-slate-900 text-lg mb-2">{{ feature.title }}</h3>
            <p class="text-slate-500 text-sm leading-relaxed">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- How it Works -->
    <section class="py-24">
      <div class="max-w-5xl mx-auto px-4">
        <div class="text-center mb-16">
          <UBadge label="Simples e rápido" color="primary" variant="subtle" class="mb-4" />
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 tracking-tight" style="font-family: 'Outfit', sans-serif">
            Comece em 3 passos
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="(step, i) in steps" :key="step.title" class="text-center">
            <div class="w-16 h-16 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 text-white flex items-center justify-center mx-auto mb-5 text-2xl font-bold shadow-lg shadow-primary-500/25" style="font-family: 'Outfit', sans-serif">
              {{ i + 1 }}
            </div>
            <h3 class="font-bold text-slate-900 mb-2">{{ step.title }}</h3>
            <p class="text-slate-500 text-sm">{{ step.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials -->
    <section class="py-24" style="background: rgba(255,255,255,0.5)">
      <div class="max-w-7xl mx-auto px-4">
        <div class="text-center mb-16">
          <UBadge label="Depoimentos" color="primary" variant="subtle" class="mb-4" />
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 tracking-tight" style="font-family: 'Outfit', sans-serif">
            Quem já usa, aprova
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            v-for="testimonial in testimonials"
            :key="testimonial.name"
            class="rounded-2xl border border-slate-200/80 bg-white/70 backdrop-blur-sm p-6"
          >
            <div class="flex gap-1 mb-4">
              <span v-for="s in 5" :key="s" class="text-yellow-400 text-lg">★</span>
            </div>
            <p class="text-slate-700 text-sm leading-relaxed mb-4 italic">"{{ testimonial.quote }}"</p>
            <div class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-primary-400 to-violet-500 flex items-center justify-center">
                <span class="text-white text-sm font-bold">{{ testimonial.name.charAt(0) }}</span>
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-900">{{ testimonial.name }}</p>
                <p class="text-xs text-slate-500">{{ testimonial.role }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Pricing -->
    <section class="py-24">
      <div class="max-w-7xl mx-auto px-4">
        <div class="text-center mb-4">
          <UBadge label="Preços" color="primary" variant="subtle" class="mb-4" />
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 tracking-tight" style="font-family: 'Outfit', sans-serif">
            Planos simples e acessíveis
          </h2>
          <p class="text-slate-500 mt-3">Comece grátis e faça upgrade quando quiser</p>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto mt-12">
          <div
            v-for="plan in plans"
            :key="plan.name"
            class="rounded-2xl border bg-white/70 backdrop-blur-sm p-8 transition-all duration-300 hover:shadow-xl hover:-translate-y-1"
            :class="plan.featured ? 'border-primary-400 ring-2 ring-primary-500/20 shadow-lg shadow-primary-500/10' : 'border-slate-200/80'"
          >
            <div v-if="plan.featured" class="text-center mb-4">
              <UBadge label="Mais popular" color="primary" />
            </div>
            <h3 class="font-bold text-xl text-slate-900 mb-1">{{ plan.name }}</h3>
            <div class="flex items-end gap-1 mb-6">
              <span class="text-4xl font-bold text-slate-900" style="font-family: 'Outfit', sans-serif">{{ plan.price }}</span>
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
                :class="plan.featured ? 'shadow-lg shadow-primary-500/25' : ''"
              />
            </NuxtLink>
          </div>
        </div>
      </div>
    </section>

    <!-- Final CTA -->
    <section class="py-24">
      <div class="max-w-3xl mx-auto px-4 text-center">
        <div class="rounded-3xl bg-gradient-to-br from-primary-600 via-primary-700 to-violet-700 p-12 md:p-16 shadow-2xl shadow-primary-500/30 relative overflow-hidden">
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.15),transparent)]" />
          <div class="relative z-10">
            <h2 class="text-3xl md:text-4xl font-bold text-white mb-4" style="font-family: 'Outfit', sans-serif">
              Pronto para começar?
            </h2>
            <p class="text-primary-100 mb-8 max-w-md mx-auto">
              Junte-se a milhares de estudantes que já estão se preparando com IA personalizada.
            </p>
            <NuxtLink to="/auth/register">
              <UButton size="xl" color="white" variant="solid" label="Criar conta grátis" trailing-icon="i-heroicons-arrow-right" class="text-primary-700" />
            </NuxtLink>
          </div>
        </div>
      </div>
      
      <!-- Background elements -->
      <div class="absolute top-1/2 left-0 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary-500/10 rounded-full blur-3xl" />
      <div class="absolute top-1/2 right-0 translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl" />
    </section>

    <!-- Footer -->
    <footer class="border-t border-slate-200/60 py-12" style="background: rgba(255,255,255,0.5)">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row items-center justify-between gap-6">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 bg-gradient-to-br from-primary-500 to-primary-700 rounded-lg flex items-center justify-center">
              <UIcon name="i-heroicons-academic-cap" class="text-white w-4 h-4" />
            </div>
            <span class="font-bold text-slate-900">MentorIA</span>
          </div>
          <div class="flex items-center gap-6 text-sm text-slate-500">
            <NuxtLink to="/terms" class="hover:text-slate-900 transition-colors">Termos de uso</NuxtLink>
            <NuxtLink to="/privacy" class="hover:text-slate-900 transition-colors">Privacidade</NuxtLink>
            <NuxtLink to="/pricing" class="hover:text-slate-900 transition-colors">Preços</NuxtLink>
          </div>
          <p class="text-sm text-slate-400">© {{ currentYear }} MentorIA. Todos os direitos reservados.</p>
        </div>
      </div>
    </footer>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

const currentYear = new Date().getFullYear()

const heroStats = [
  { value: '5.000+', label: 'Alunos ativos' },
  { value: '50.000+', label: 'Mensagens trocadas' },
  { value: '98%', label: 'Satisfação' },
]

const features = [
  {
    icon: 'i-heroicons-chat-bubble-left-right',
    title: 'Chat com Mentor IA',
    description: 'Tire dúvidas a qualquer hora com um mentor personalizado para o seu concurso. Respostas contextualizadas e didáticas.',
    bgClass: 'bg-blue-100',
    iconClass: 'text-blue-600',
  },
  {
    icon: 'i-heroicons-book-open',
    title: 'Plano de Estudos Adaptativo',
    description: 'Receba um cronograma que se adapta ao seu nível, disponibilidade e objetivo. Atualizado com base no seu progresso.',
    bgClass: 'bg-violet-100',
    iconClass: 'text-violet-600',
  },
  {
    icon: 'i-heroicons-chart-bar',
    title: 'Acompanhamento Inteligente',
    description: 'Visualize seu progresso, mantenha a sequência de estudos, ganhe XP e desbloqueie conquistas motivadoras.',
    bgClass: 'bg-emerald-100',
    iconClass: 'text-emerald-600',
  },
]

const steps = [
  {
    title: 'Crie sua conta grátis',
    description: 'Em menos de 1 minuto, sem necessidade de cartão de crédito.',
  },
  {
    title: 'Configure seu perfil',
    description: 'Diga qual concurso quer prestar, seu nível e disponibilidade.',
  },
  {
    title: 'Comece a estudar com IA',
    description: 'Receba seu plano personalizado e converse com seu mentor.',
  },
]

const testimonials = [
  {
    name: 'Ana Carolina',
    role: 'Aprovada no INSS 2025',
    quote: 'O MentorIA mudou minha rotina de estudos. O plano personalizado me ajudou a focar nas matérias certas e passei na primeira tentativa!',
  },
  {
    name: 'Lucas Ferreira',
    role: 'Estudante de Direito',
    quote: 'Ter um mentor disponível 24h é incrível. Estudo de madrugada e sempre tenho respostas claras e didáticas na hora que preciso.',
  },
  {
    name: 'Maria Santos',
    role: 'Concurseira — TRT',
    quote: 'A gamificação me mantém motivada. O sistema de XP e sequência faz com que eu não queira parar de estudar nenhum dia!',
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

const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes blob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(20px, -30px) scale(1.05); }
  50% { transform: translate(-10px, 20px) scale(0.95); }
  75% { transform: translate(15px, 10px) scale(1.02); }
}
@keyframes gradient-shift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.animate-fade-in {
  animation: fade-in 0.6s ease-out both;
}
.animate-fade-in-up {
  animation: fade-in-up 0.6s ease-out both;
}
.animate-blob {
  animation: blob 8s ease-in-out infinite;
}
.animate-gradient {
  animation: gradient-shift 4s ease-in-out infinite;
}
</style>