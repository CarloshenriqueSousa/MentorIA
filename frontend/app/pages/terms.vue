<template>
  <div class="max-w-3xl mx-auto py-12 px-4">
    <NuxtLink to="/" class="flex items-center gap-2 mb-8">
      <UIcon name="i-heroicons-arrow-left" class="w-4 h-4" />
      <span class="text-sm text-slate-600">Voltar</span>
    </NuxtLink>
    <h1 class="text-3xl font-bold text-slate-900 mb-2">Termos de Uso</h1>
    <p class="text-slate-500 mb-8">Última atualização: Janeiro 2025</p>
    <div class="prose prose-slate max-w-none space-y-6">
      <UCard v-for="section in sections" :key="section.title">
        <template #header>
          <h2 class="font-semibold text-slate-900">{{ section.title }}</h2>
        </template>
        <p class="text-sm text-slate-600 leading-relaxed">{{ section.content }}</p>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

const sections = [
  { title: '1. Aceitação dos Termos', content: 'Ao utilizar o MentorIA, você concorda com estes Termos de Uso. Se não concordar, não utilize o serviço.' },
  { title: '2. Descrição do Serviço', content: 'O MentorIA é uma plataforma de mentoria educacional baseada em Inteligência Artificial, focada em auxiliar estudantes de concursos públicos brasileiros.' },
  { title: '3. Uso Aceitável', content: 'Você concorda em usar o serviço apenas para fins educacionais legítimos, não tentando burlar limites do sistema ou utilizar a plataforma de forma prejudicial a outros usuários.' },
  { title: '4. Privacidade', content: 'Seus dados são tratados de acordo com nossa Política de Privacidade e com a Lei Geral de Proteção de Dados (LGPD). Não vendemos seus dados a terceiros.' },
  { title: '5. Pagamentos', content: 'Os pagamentos são processados de forma segura pelo Stripe. As assinaturas são renovadas automaticamente e podem ser canceladas a qualquer momento.' },
  { title: '6. Limitação de Responsabilidade', content: 'O MentorIA é uma ferramenta de apoio aos estudos. Não garantimos aprovação em concursos públicos, mas nos comprometemos a oferecer o melhor suporte possível.' },
]
</script>