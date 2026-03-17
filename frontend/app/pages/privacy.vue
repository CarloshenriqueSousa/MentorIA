<template>
  <div class="max-w-3xl mx-auto py-12 px-4">
    <NuxtLink to="/" class="flex items-center gap-2 mb-8">
      <UIcon name="i-heroicons-arrow-left" class="w-4 h-4" />
      <span class="text-sm text-slate-600">Voltar</span>
    </NuxtLink>
    <h1 class="text-3xl font-bold text-slate-900 mb-2">Política de Privacidade</h1>
    <p class="text-slate-500 mb-8">Última atualização: Janeiro 2025</p>
    <div class="space-y-4">
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
  { title: 'Dados que coletamos', content: 'Coletamos nome, email, dados de uso da plataforma e informações de estudo que você fornece voluntariamente durante o onboarding e uso do serviço.' },
  { title: 'Como usamos seus dados', content: 'Usamos seus dados exclusivamente para personalizar sua experiência de aprendizado, melhorar nosso serviço e processar pagamentos. Nunca vendemos seus dados.' },
  { title: 'Segurança', content: 'Utilizamos criptografia de ponta e seguimos as melhores práticas de segurança para proteger suas informações. Seus dados são armazenados em servidores seguros.' },
  { title: 'Seus direitos (LGPD)', content: 'Você tem direito de acessar, corrigir ou excluir seus dados a qualquer momento. Entre em contato conosco pelo email privacidade@mentoria.app.' },
  { title: 'Cookies', content: 'Utilizamos cookies essenciais para manter sua sessão ativa e cookies analíticos (anonimizados) para melhorar o serviço. Você pode desativá-los nas configurações do navegador.' },
]
</script>