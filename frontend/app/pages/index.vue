<template>
  <div class="min-h-screen bg-white dark:bg-slate-950 transition-colors duration-500">

    <!-- Navbar -->
    <header class="sticky top-0 z-50 bg-white/80 dark:bg-slate-950/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 transition-all">
      <div class="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
        <div class="flex items-center gap-3 group cursor-pointer">
          <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center group-hover:rotate-12 transition-transform shadow-lg shadow-primary-500/20">
            <UIcon name="i-heroicons-academic-cap" class="text-white w-6 h-6" />
          </div>
          <span class="font-black text-slate-900 dark:text-white text-2xl font-outfit tracking-tight">MentorIA</span>
        </div>
        
        <div class="flex items-center gap-4">
          <!-- Dark Mode Toggle -->
          <UButton
            :icon="isDark ? 'i-heroicons-moon' : 'i-heroicons-sun'"
            variant="ghost"
            color="neutral"
            size="sm"
            class="rounded-full"
            @click="toggleColorMode"
          />
          
          <div class="hidden md:flex items-center gap-2">
            <NuxtLink to="/auth/login">
              <UButton variant="ghost" color="neutral" label="Entrar" class="font-semibold" />
            </NuxtLink>
            <NuxtLink to="/auth/register">
              <UButton label="Começar grátis" class="font-bold px-6 rounded-xl shadow-lg shadow-primary-500/20" />
            </NuxtLink>
          </div>
          
          <UButton icon="i-heroicons-bars-3" variant="ghost" color="neutral" class="md:hidden" />
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section class="max-w-7xl mx-auto px-4 py-32 text-center relative overflow-hidden">
      <!-- Glow effects -->
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[600px] bg-primary-500/10 dark:bg-primary-500/5 blur-[120px] rounded-full pointer-events-none -z-10" />
      
      <div class="animate-fade-in-up">
        <UBadge label="🚀 Novo: Planos de estudo com IA" color="primary" variant="subtle" class="mb-8 px-4 py-1.5 rounded-full text-sm font-bold tracking-wide uppercase" />
        
        <h1 class="text-6xl md:text-7xl font-black text-slate-900 dark:text-white mb-8 leading-[1.1] font-outfit tracking-tight">
          Seu mentor de estudos <br >
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-primary-600 to-indigo-600 dark:from-primary-400 dark:to-indigo-400">
            disponível 24 horas
          </span>
        </h1>
        
        <p class="text-xl md:text-2xl text-slate-600 dark:text-slate-400 mb-12 max-w-3xl mx-auto leading-relaxed">
          Prepare-se para concursos públicos com um mentor de IA personalizado. Planos de estudo adaptativos, chat interativo e acompanhamento de progresso.
        </p>
        
        <div class="flex items-center justify-center gap-6 flex-wrap">
          <NuxtLink to="/auth/register">
            <UButton 
              size="xl" 
              label="Começar grátis agora" 
              trailing-icon="i-heroicons-arrow-right" 
              class="px-10 py-4 text-lg font-bold rounded-2xl shadow-2xl shadow-primary-500/30 hover:scale-105 active:scale-95 transition-all"
            />
          </NuxtLink>
          <UButton 
            size="xl" 
            variant="outline" 
            color="neutral" 
            label="Ver como funciona" 
            class="px-10 py-4 text-lg font-bold rounded-2xl dark:border-slate-700 dark:text-slate-300"
          />
        </div>
        
        <div class="flex items-center justify-center gap-8 mt-12 text-slate-400 dark:text-slate-600 font-medium">
          <div class="flex items-center gap-2">
            <UIcon name="i-heroicons-check-circle" class="text-green-500" />
            <span>Grátis para começar</span>
          </div>
          <div class="flex items-center gap-2">
            <UIcon name="i-heroicons-check-circle" class="text-green-500" />
            <span>Sem cartão de crédito</span>
          </div>
        </div>
      </div>
      
      <!-- Decorative UI Elements -->
      <div class="mt-24 relative max-w-5xl mx-auto group">
        <div class="absolute inset-0 bg-gradient-to-b from-primary-500/20 to-transparent blur-3xl -z-10 group-hover:from-primary-500/30 transition-all duration-700" />
        <div class="rounded-3xl border border-slate-200 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50 backdrop-blur shadow-2xl overflow-hidden aspect-[16/9] flex items-center justify-center">
            <div class="text-slate-300 dark:text-slate-700 flex flex-col items-center">
                <UIcon name="i-heroicons-play-circle" class="w-24 h-24 mb-4 opacity-50" />
                <p class="font-bold text-xl uppercase tracking-widest font-outfit">Visualização do App</p>
            </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="bg-slate-50 dark:bg-slate-900/50 py-32 transition-colors">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <h2 class="text-4xl md:text-5xl font-black text-slate-900 dark:text-white mb-6 font-outfit">
          Tudo que você precisa para passar
        </h2>
        <p class="text-xl text-slate-500 dark:text-slate-400 mb-20 max-w-2xl mx-auto">
          Ferramentas inteligentes desenhadas para maximizar sua retenção e eficiência nos estudos.
        </p>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
          <div 
            v-for="(feature, idx) in features" 
            :key="feature.title" 
            class="p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-500 group text-left"
          >
            <div :class="`w-14 h-14 ${feature.bg} rounded-2xl flex items-center justify-center mb-8 group-hover:scale-110 transition-transform`">
              <UIcon :name="feature.icon" :class="`${feature.color} w-8 h-8`" />
            </div>
            <h3 class="text-2xl font-bold text-slate-900 dark:text-white mb-4 font-outfit">{{ feature.title }}</h3>
            <p class="text-slate-500 dark:text-slate-400 leading-relaxed">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA & Social Proof -->
    <section class="py-32 overflow-hidden relative">
      <div class="max-w-4xl mx-auto px-4 text-center relative z-10">
        <h2 class="text-5xl font-black text-slate-900 dark:text-white mb-8 font-outfit">Junte-se a milhares de concurseiros</h2>
        <p class="text-xl text-slate-600 dark:text-slate-400 mb-12">A tecnologia que você precisava para organizar sua rotina e manter a constância nos estudos.</p>
        <NuxtLink to="/auth/register">
            <UButton size="xl" label="Criar minha conta gratuita" class="px-12 py-5 text-xl font-bold rounded-2xl shadow-2xl shadow-primary-500/40" />
        </NuxtLink>
      </div>
      
      <!-- Background elements -->
      <div class="absolute top-1/2 left-0 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary-500/10 rounded-full blur-3xl" />
      <div class="absolute top-1/2 right-0 translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl" />
    </section>

    <!-- Footer -->
    <footer class="border-t border-slate-200 dark:border-slate-800 py-20 bg-white dark:bg-slate-950">
      <div class="max-w-7xl mx-auto px-4 grid grid-cols-1 md:grid-cols-4 gap-12">
        <div class="col-span-1 md:col-span-2">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <UIcon name="i-heroicons-academic-cap" class="text-white w-5 h-5" />
            </div>
            <span class="font-bold text-slate-900 dark:text-white text-2xl font-outfit">MentorIA</span>
          </div>
          <p class="text-slate-500 dark:text-slate-400 max-w-sm leading-relaxed">
            Acelerando aprovações através de inteligência artificial generativa e metodologias de estudo comprovadas.
          </p>
        </div>
        
        <div>
          <h4 class="font-bold text-slate-900 dark:text-white mb-6 uppercase tracking-wider text-sm">Produto</h4>
          <ul class="space-y-4 text-slate-500 dark:text-slate-400 font-medium">
            <li><NuxtLink class="hover:text-primary-600 transition-colors">Funcionalidades</NuxtLink></li>
            <li><NuxtLink class="hover:text-primary-600 transition-colors">Preços</NuxtLink></li>
            <li><NuxtLink class="hover:text-primary-600 transition-colors">Agentes IA</NuxtLink></li>
          </ul>
        </div>
        
        <div>
          <h4 class="font-bold text-slate-900 dark:text-white mb-6 uppercase tracking-wider text-sm">Legal</h4>
          <ul class="space-y-4 text-slate-500 dark:text-slate-400 font-medium">
            <li><NuxtLink to="/terms" class="hover:text-primary-600 transition-colors">Termos de uso</NuxtLink></li>
            <li><NuxtLink to="/privacy" class="hover:text-primary-600 transition-colors">Privacidade</NuxtLink></li>
            <li class="pt-4 text-slate-400 dark:text-slate-600 font-normal">© 2026 MentorIA</li>
          </ul>
        </div>
      </div>
    </footer>

  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

const colorMode = useColorMode()
const isDark = computed(() => colorMode.value === 'dark')

const toggleColorMode = () => {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}

const features = [
  {
    icon: 'i-heroicons-chat-bubble-left-right',
    title: 'Chat com Mentor IA',
    description: 'Tire dúvidas a qualquer hora com um mentor especializado para o seu concurso com respostas instantâneas.',
    bg: 'bg-blue-50 dark:bg-blue-500/10',
    color: 'text-blue-600 dark:text-blue-400',
  },
  {
    icon: 'i-heroicons-book-open',
    title: 'Plano de Estudos',
    description: 'Receba um cronograma adaptado ao seu nível, disponibilidade e objetivo de forma 100% automática.',
    bg: 'bg-purple-50 dark:bg-purple-500/10',
    color: 'text-purple-600 dark:text-purple-400',
  },
  {
    icon: 'i-heroicons-chart-bar',
    title: 'Acompanhamento',
    description: 'Visualize seu progresso, mantenha a sequência de estudos e ganhe recompensas gamificadas.',
    bg: 'bg-emerald-50 dark:bg-emerald-500/10',
    color: 'text-emerald-600 dark:text-emerald-400',
  },
]
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap');

.font-outfit {
  font-family: 'Outfit', sans-serif;
}

.animate-fade-in-up {
  animation: fadeInUp 1s ease-out forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-bounce-slow {
  animation: bounce 4s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(-5%);
    animation-timing-function: cubic-bezier(0.8, 0, 1, 1);
  }
  50% {
    transform: translateY(0);
    animation-timing-function: cubic-bezier(0, 0, 0.2, 1);
  }
}
</style>