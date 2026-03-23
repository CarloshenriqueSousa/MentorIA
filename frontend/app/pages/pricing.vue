<template>
  <div class="space-y-6">

    <div class="text-center">
      <h1 class="text-2xl font-bold text-slate-900">Escolha seu plano</h1>
      <p class="text-slate-500 mt-1">Faça upgrade para desbloquear todo o potencial do MentorIA</p>
    </div>

    <!-- Plano atual -->
    <UCard v-if="authStore.user" class="bg-primary-50 border-primary-200">
      <div class="flex items-center gap-3">
        <UIcon name="i-heroicons-information-circle" class="text-primary-600 w-5 h-5" />
        <p class="text-sm text-primary-800">
          Plano atual: <strong>{{ authStore.user.planType }}</strong>
          <span v-if="authStore.user.planType === 'FREE'"> — 10 mensagens por dia</span>
          <span v-else> — Mensagens ilimitadas</span>
        </p>
      </div>
    </UCard>

    <!-- Cards de planos -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <UCard
        v-for="plan in plans"
        :key="plan.id"
        :class="plan.featured ? 'ring-2 ring-primary-600' : ''"
      >
        <template #header>
          <div class="text-center">
            <div v-if="plan.featured" class="mb-3">
              <UBadge label="Mais popular" color="primary" />
            </div>
            <h2 class="text-xl font-bold text-slate-900">{{ plan.name }}</h2>
            <div class="flex items-end justify-center gap-1 mt-2">
              <span class="text-4xl font-bold text-slate-900">{{ plan.price }}</span>
              <span class="text-slate-500 mb-1">/mês</span>
            </div>
          </div>
        </template>

        <ul class="space-y-3">
          <li
            v-for="feature in plan.features"
            :key="feature.text"
            class="flex items-start gap-2 text-sm"
          >
            <UIcon
              :name="feature.included ? 'i-heroicons-check-circle' : 'i-heroicons-x-circle'"
              :class="feature.included ? 'text-green-500' : 'text-slate-300'"
              class="w-5 h-5 flex-shrink-0 mt-0.5"
            />
            <span :class="feature.included ? 'text-slate-700' : 'text-slate-400'">
              {{ feature.text }}
            </span>
          </li>
        </ul>

        <template #footer>
          <UButton
            v-if="plan.id === 'free'"
            block
            variant="outline"
            color="neutral"
            label="Plano atual"
            disabled
          />
          <UButton
            v-else-if="authStore.user?.planType === plan.id.toUpperCase()"
            block
            variant="outline"
            color="success"
            label="✓ Plano ativo"
            disabled
          />
          <UButton
            v-else
            block
            :variant="plan.featured ? 'solid' : 'outline'"
            :color="plan.featured ? 'primary' : 'neutral'"
            :label="plan.cta"
            :loading="checkingOut === plan.id"
            @click="checkout(plan.id)"
          />
        </template>
      </UCard>
    </div>

    <!-- FAQ -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Perguntas frequentes</h2>
      </template>
      <UAccordion :items="faq" />
    </UCard>

  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: 'default', middleware: 'auth' })

const authStore = useAuthStore()
const { post } = useApi()
const toast = useToast()

const checkingOut = ref<string | null>(null)

const plans = [
  {
    id: 'free',
    name: 'Grátis',
    price: 'R$0',
    featured: false,
    cta: 'Plano atual',
    features: [
      { text: '10 mensagens por dia', included: true },
      { text: '2 planos de estudo', included: true },
      { text: 'Dashboard básico', included: true },
      { text: 'Mensagens ilimitadas', included: false },
      { text: 'Histórico completo', included: false },
      { text: 'Suporte prioritário', included: false },
    ],
  },
  {
    id: 'basic',
    name: 'Básico',
    price: 'R$29',
    featured: true,
    cta: 'Assinar Básico',
    features: [
      { text: 'Mensagens ilimitadas', included: true },
      { text: 'Planos ilimitados', included: true },
      { text: 'Dashboard completo', included: true },
      { text: 'Histórico completo', included: true },
      { text: 'Suporte prioritário', included: true },
      { text: 'Relatórios avançados', included: false },
    ],
  },
  {
    id: 'premium',
    name: 'Premium',
    price: 'R$49',
    featured: false,
    cta: 'Assinar Premium',
    features: [
      { text: 'Tudo do Básico', included: true },
      { text: 'Relatórios avançados', included: true },
      { text: 'Gamificação completa', included: true },
      { text: 'Acesso antecipado', included: true },
      { text: 'Suporte VIP', included: true },
      { text: 'Mentor híbrido (em breve)', included: true },
    ],
  },
]

const faq = [
  { label: 'Posso cancelar a qualquer momento?', content: 'Sim! Você pode cancelar quando quiser sem multa. O acesso continua até o fim do período pago.' },
  { label: 'Como funciona o pagamento?', content: 'O pagamento é feito via cartão de crédito ou Pix, processado com segurança pelo Stripe.' },
  { label: 'Meus dados estão seguros?', content: 'Sim. Seguimos todas as normas da LGPD e nunca compartilhamos seus dados com terceiros.' },
]

type CheckoutResponse = {
  url: string
}

const checkout = async (planId: string) => {
  checkingOut.value = planId
  try {
    const response = await post<CheckoutResponse>('/payments/checkout', {
      plan: planId.toUpperCase(),
      successUrl: `${window.location.origin}/dashboard?upgrade=success`,
      cancelUrl: `${window.location.origin}/pricing`,
    })
    window.location.href = response.url
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao iniciar checkout'
    toast.add({ title: 'Erro ao iniciar checkout', description: message, color: 'error' })
  } finally {
    checkingOut.value = null
  }
}
</script>