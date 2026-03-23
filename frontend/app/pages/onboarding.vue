<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-4">
    <div class="w-full max-w-2xl">

      <!-- Header -->
      <div class="text-center mb-8">
        <div class="w-12 h-12 bg-primary-600 rounded-xl flex items-center justify-center mx-auto mb-4">
          <UIcon name="i-heroicons-academic-cap" class="text-white w-7 h-7" />
        </div>
        <h1 class="text-2xl font-bold text-slate-900">Vamos configurar seu perfil</h1>
        <p class="text-slate-500 mt-2">Essas informações ajudam o MentorIA a personalizar seus estudos</p>
      </div>

      <!-- Progress -->
      <div class="flex items-center gap-2 mb-8">
        <div
          v-for="i in totalSteps"
          :key="i"
          class="flex-1 h-2 rounded-full transition-colors"
          :class="i <= currentStep ? 'bg-primary-600' : 'bg-slate-200'"
        />
      </div>

      <UCard>
        <!-- Step 1: Concurso alvo -->
        <div v-if="currentStep === 1" class="space-y-6">
          <div>
            <h2 class="text-lg font-semibold text-slate-900 mb-1">Qual concurso você está estudando?</h2>
            <p class="text-slate-500 text-sm">Ex: INSS 2025, Banco do Brasil, Polícia Federal</p>
          </div>
          <UInput
            v-model="form.targetExam"
            placeholder="Digite o concurso alvo"
            icon="i-heroicons-trophy"
            size="lg"
          />
          <div>
            <p class="text-sm font-medium text-slate-700 mb-3">Sugestões populares:</p>
            <div class="flex flex-wrap gap-2">
              <UBadge
                v-for="exam in popularExams"
                :key="exam"
                :label="exam"
                variant="outline"
                color="neutral"
                class="cursor-pointer hover:bg-slate-100"
                @click="form.targetExam = exam"
              />
            </div>
          </div>
        </div>

        <!-- Step 2: Nível -->
        <div v-if="currentStep === 2" class="space-y-6">
          <div>
            <h2 class="text-lg font-semibold text-slate-900 mb-1">Qual é o seu nível atual?</h2>
            <p class="text-slate-500 text-sm">Isso ajuda a calibrar a dificuldade das respostas</p>
          </div>
          <div class="grid grid-cols-1 gap-3">
            <div
              v-for="level in levels"
              :key="level.value"
              class="flex items-center gap-4 p-4 rounded-xl border-2 cursor-pointer transition-colors"
              :class="form.knowledgeLevel === level.value
                ? 'border-primary-600 bg-primary-50'
                : 'border-slate-200 hover:border-slate-300'"
              @click="form.knowledgeLevel = level.value"
            >
              <span class="text-2xl">{{ level.emoji }}</span>
              <div>
                <p class="font-medium text-slate-900">{{ level.label }}</p>
                <p class="text-sm text-slate-500">{{ level.description }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Disponibilidade -->
        <div v-if="currentStep === 3" class="space-y-6">
          <div>
            <h2 class="text-lg font-semibold text-slate-900 mb-1">Quanto tempo você tem para estudar?</h2>
            <p class="text-slate-500 text-sm">Selecione seus dias e horas disponíveis</p>
          </div>
          <div>
            <p class="text-sm font-medium text-slate-700 mb-3">Dias disponíveis:</p>
            <div class="flex flex-wrap gap-2">
              <UBadge
                v-for="day in days"
                :key="day.value"
                :label="day.label"
                :color="form.availableDays.includes(day.value) ? 'primary' : 'neutral'"
                :variant="form.availableDays.includes(day.value) ? 'solid' : 'outline'"
                class="cursor-pointer"
                @click="toggleDay(day.value)"
              />
            </div>
          </div>
          <div>
            <p class="text-sm font-medium text-slate-700 mb-3">Horas por dia: <span class="text-primary-600 font-bold">{{ form.studyHoursPerDay }}h</span></p>
            <input
              v-model="form.studyHoursPerDay"
              type="range"
              min="1"
              max="12"
              class="w-full accent-primary-600"
            />
            <div class="flex justify-between text-xs text-slate-400 mt-1">
              <span>1h</span>
              <span>12h</span>
            </div>
          </div>
        </div>

        <!-- Step 4: Dificuldades e forças -->
        <div v-if="currentStep === 4" class="space-y-6">
          <div>
            <h2 class="text-lg font-semibold text-slate-900 mb-1">Suas dificuldades e pontos fortes</h2>
            <p class="text-slate-500 text-sm">Selecione as matérias que você tem dificuldade e facilidade</p>
          </div>
          <div>
            <p class="text-sm font-medium text-red-600 mb-3">⚠️ Tenho dificuldade em:</p>
            <div class="flex flex-wrap gap-2">
              <UBadge
                v-for="subject in subjects"
                :key="subject"
                :label="subject"
                :color="form.difficulties.includes(subject) ? 'error' : 'neutral'"
                :variant="form.difficulties.includes(subject) ? 'solid' : 'outline'"
                class="cursor-pointer"
                @click="toggleItem(form.difficulties, subject)"
              />
            </div>
          </div>
          <div>
            <p class="text-sm font-medium text-green-600 mb-3">💪 Tenho facilidade em:</p>
            <div class="flex flex-wrap gap-2">
              <UBadge
                v-for="subject in subjects"
                :key="subject"
                :label="subject"
                :color="form.strengths.includes(subject) ? 'success' : 'neutral'"
                :variant="form.strengths.includes(subject) ? 'solid' : 'outline'"
                class="cursor-pointer"
                @click="toggleItem(form.strengths, subject)"
              />
            </div>
          </div>
        </div>

        <!-- Step 5: Objetivos -->
        <div v-if="currentStep === 5" class="space-y-6">
          <div>
            <h2 class="text-lg font-semibold text-slate-900 mb-1">Quais são seus objetivos?</h2>
            <p class="text-slate-500 text-sm">Selecione todos que se aplicam</p>
          </div>
          <div class="grid grid-cols-1 gap-3">
            <div
              v-for="obj in objectives"
              :key="obj"
              class="flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-colors"
              :class="form.objectives.includes(obj)
                ? 'border-primary-600 bg-primary-50'
                : 'border-slate-200 hover:border-slate-300'"
              @click="toggleItem(form.objectives, obj)"
            >
              <UIcon
                :name="form.objectives.includes(obj) ? 'i-heroicons-check-circle' : 'i-heroicons-circle'"
                :class="form.objectives.includes(obj) ? 'text-primary-600' : 'text-slate-300'"
                class="w-5 h-5 flex-shrink-0"
              />
              <span class="text-sm text-slate-700">{{ obj }}</span>
            </div>
          </div>
        </div>

        <!-- Navigation -->
        <div class="flex items-center justify-between mt-8 pt-6 border-t border-slate-100">
          <UButton
            v-if="currentStep > 1"
            variant="ghost"
            color="neutral"
            label="Voltar"
            icon="i-heroicons-arrow-left"
            @click="currentStep--"
          />
          <div v-else />

          <UButton
            v-if="currentStep < totalSteps"
            label="Próximo"
            trailing-icon="i-heroicons-arrow-right"
            :disabled="!canProceed"
            @click="currentStep++"
          />
          <UButton
            v-else
            label="Concluir e começar!"
            trailing-icon="i-heroicons-rocket-launch"
            :loading="loading"
            :disabled="!canProceed"
            @click="submitOnboarding"
          />
        </div>
      </UCard>

    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: false })

const authStore = useAuthStore()
const router = useRouter()
const { post } = useApi()
const toast = useToast()

const currentStep = ref(1)
const totalSteps = 5
const loading = ref(false)

const form = reactive({
  targetExam: '',
  knowledgeLevel: '',
  studyHoursPerDay: 3,
  availableDays: [] as string[],
  difficulties: [] as string[],
  strengths: [] as string[],
  objectives: [] as string[],
})

const popularExams = ['INSS', 'Banco do Brasil', 'Polícia Federal', 'Receita Federal', 'TRT', 'ENEM']

const levels = [
  { value: 'BEGINNER', emoji: '🌱', label: 'Iniciante', description: 'Estou começando agora' },
  { value: 'INTERMEDIATE', emoji: '📚', label: 'Intermediário', description: 'Já tenho uma base sólida' },
  { value: 'ADVANCED', emoji: '🎯', label: 'Avançado', description: 'Estou na fase de revisão final' },
]

const days = [
  { value: 'SEG', label: 'Seg' },
  { value: 'TER', label: 'Ter' },
  { value: 'QUA', label: 'Qua' },
  { value: 'QUI', label: 'Qui' },
  { value: 'SEX', label: 'Sex' },
  { value: 'SAB', label: 'Sáb' },
  { value: 'DOM', label: 'Dom' },
]

const subjects = [
  'Português', 'Matemática', 'Raciocínio Lógico', 'Direito Constitucional',
  'Direito Administrativo', 'Informática', 'Atualidades', 'Contabilidade',
]

const objectives = [
  'Ser aprovado no concurso alvo',
  'Melhorar em matérias específicas',
  'Manter uma rotina de estudos consistente',
  'Resolver mais questões práticas',
  'Revisar conteúdos já estudados',
]

const canProceed = computed(() => {
  if (currentStep.value === 1) return form.targetExam.trim().length > 0
  if (currentStep.value === 2) return form.knowledgeLevel !== ''
  if (currentStep.value === 3) return form.availableDays.length > 0
  if (currentStep.value === 4) return true
  if (currentStep.value === 5) return form.objectives.length > 0
  return true
})

const toggleDay = (day: string) => {
  const idx = form.availableDays.indexOf(day)
  if (idx > -1) form.availableDays.splice(idx, 1)
  else form.availableDays.push(day)
}

const toggleItem = (arr: string[], item: string) => {
  const idx = arr.indexOf(item)
  if (idx > -1) arr.splice(idx, 1)
  else arr.push(item)
}

const submitOnboarding = async () => {
  loading.value = true
  try {
    await post('/onboarding', form)
    authStore.updateUser({ completedOnboarding: true })
    toast.add({ title: 'Perfil configurado!', description: 'Bem-vindo ao MentorIA 🎉', color: 'success' })
    router.push('/dashboard')
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao enviar onboarding'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    loading.value = false
  }
}
</script>