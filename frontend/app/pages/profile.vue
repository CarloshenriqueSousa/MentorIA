<template>
  <div class="space-y-6 max-w-2xl">

    <div>
      <h1 class="text-2xl font-bold text-slate-900">Meu Perfil</h1>
      <p class="text-slate-500 mt-1">Gerencie suas informações pessoais</p>
    </div>

    <!-- Info pessoal -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Informações pessoais</h2>
      </template>

      <div class="flex items-center gap-4 mb-6">
        <UAvatar :alt="authStore.user?.name" size="xl" />
        <div>
          <p class="font-semibold text-slate-900">{{ authStore.user?.name }}</p>
          <p class="text-sm text-slate-500">{{ authStore.user?.email }}</p>
          <UBadge
            :label="authStore.user?.planType"
            :color="authStore.user?.planType === 'FREE' ? 'neutral' : 'primary'"
            variant="subtle"
            class="mt-1"
          />
        </div>
      </div>

      <UForm :state="profileForm" class="space-y-4" @submit="saveProfile">
        <UFormField label="Nome completo">
          <UInput v-model="profileForm.name" icon="i-heroicons-user" class="w-full" />
        </UFormField>
        <UFormField label="Email">
          <UInput v-model="profileForm.email" type="email" icon="i-heroicons-envelope" class="w-full" disabled />
        </UFormField>
        <UButton type="submit" label="Salvar alterações" :loading="savingProfile" />
      </UForm>
    </UCard>

    <!-- Alterar senha -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-slate-900">Alterar senha</h2>
      </template>
      <UForm :state="passwordForm" class="space-y-4" @submit="changePassword">
        <UFormField label="Nova senha">
          <UInput v-model="passwordForm.newPassword" type="password" icon="i-heroicons-lock-closed" class="w-full" />
        </UFormField>
        <UFormField label="Confirmar nova senha">
          <UInput v-model="passwordForm.confirmPassword" type="password" icon="i-heroicons-lock-closed" class="w-full" />
        </UFormField>
        <UButton type="submit" label="Alterar senha" variant="outline" :loading="savingPassword" />
      </UForm>
    </UCard>

    <!-- Perfil de estudos -->
    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <h2 class="font-semibold text-slate-900">Perfil de estudos</h2>
          <NuxtLink to="/onboarding">
            <UButton size="xs" variant="ghost" label="Editar" icon="i-heroicons-pencil" />
          </NuxtLink>
        </div>
      </template>

      <div v-if="studyProfile" class="space-y-3">
        <div class="flex items-center gap-2">
          <UIcon name="i-heroicons-trophy" class="text-primary-500 w-4 h-4" />
          <span class="text-sm text-slate-600">Concurso alvo: <strong>{{ studyProfile.targetExam }}</strong></span>
        </div>
        <div class="flex items-center gap-2">
          <UIcon name="i-heroicons-chart-bar" class="text-primary-500 w-4 h-4" />
          <span class="text-sm text-slate-600">Nível: <strong>{{ studyProfile.knowledgeLevel }}</strong></span>
        </div>
        <div class="flex items-center gap-2">
          <UIcon name="i-heroicons-clock" class="text-primary-500 w-4 h-4" />
          <span class="text-sm text-slate-600">Estudo diário: <strong>{{ studyProfile.studyHoursPerDay }}h</strong></span>
        </div>
      </div>
    </UCard>

    <!-- Zona de perigo -->
    <UCard class="border-red-200">
      <template #header>
        <h2 class="font-semibold text-red-600">Zona de perigo</h2>
      </template>
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-slate-900">Excluir conta</p>
          <p class="text-xs text-slate-500">Esta ação é irreversível</p>
        </div>
        <UButton label="Excluir conta" color="error" variant="outline" size="sm" @click="showDeleteModal = true" />
      </div>
    </UCard>

    <!-- Modal de confirmação de exclusão -->
    <UModal v-model:open="showDeleteModal">
      <template #content>
        <UCard>
          <template #header>
            <h3 class="font-semibold text-red-600">Excluir conta permanentemente</h3>
          </template>
          <div class="space-y-4">
            <p class="text-sm text-slate-600">
              Tem certeza que deseja excluir sua conta? Todos os seus dados serão removidos permanentemente.
            </p>
            <UFormField label="Digite 'EXCLUIR' para confirmar">
              <UInput v-model="deleteConfirmText" placeholder="EXCLUIR" class="w-full" />
            </UFormField>
          </div>
          <template #footer>
            <div class="flex justify-end gap-2">
              <UButton variant="ghost" label="Cancelar" @click="showDeleteModal = false" />
              <UButton
                label="Excluir minha conta"
                color="error"
                :loading="deletingAccount"
                :disabled="deleteConfirmText !== 'EXCLUIR'"
                @click="deleteAccount"
              />
            </div>
          </template>
        </UCard>
      </template>
    </UModal>

  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ layout: 'default', middleware: 'auth' })

const authStore = useAuthStore()
<<<<<<< Updated upstream
const { get, put } = useApi()
=======
const { get, put, del } = useApi()
const supabase = useSupabaseClient()
>>>>>>> Stashed changes
const toast = useToast()
const router = useRouter()

const savingProfile = ref(false)
const savingPassword = ref(false)
const showDeleteModal = ref(false)
const deletingAccount = ref(false)
const deleteConfirmText = ref('')

type StudyProfile = {
  targetExam: string
  knowledgeLevel: string
  studyHoursPerDay: number
}

const studyProfile = ref<StudyProfile | null>(null)

const profileForm = reactive({
  name: authStore.user?.name || '',
  email: authStore.user?.email || '',
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const saveProfile = async () => {
  savingProfile.value = true
  try {
<<<<<<< Updated upstream
    await put('/users/profile', { name: profileForm.name })
    authStore.updateUser({ name: profileForm.name })
    toast.add({ title: 'Perfil atualizado!', color: 'success' })
  } catch (error: any) {
    toast.add({ 
      title: 'Erro ao atualizar perfil', 
      description: error.message || 'Erro desconhecido', 
      color: 'error' 
    })
=======
    await put('/users/me', { name: profileForm.name })
    authStore.updateUser({ name: profileForm.name })
    toast.add({ title: 'Perfil atualizado!', color: 'success' })
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao salvar perfil'
    toast.add({ title: 'Erro', description: message, color: 'error' })
>>>>>>> Stashed changes
  } finally {
    savingProfile.value = false
  }
}

const changePassword = async () => {
  if (passwordForm.newPassword.length < 6) {
    toast.add({ title: 'A senha deve ter pelo menos 6 caracteres', color: 'error' })
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.add({ title: 'Senhas não coincidem', color: 'error' })
    return
  }
  savingPassword.value = true
  try {
<<<<<<< Updated upstream
    await put('/users/password', {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
=======
    const { error } = await supabase.auth.updateUser({
      password: passwordForm.newPassword,
    })
    if (error) {
      throw new Error(error.message)
    }
>>>>>>> Stashed changes
    toast.add({ title: 'Senha alterada com sucesso!', color: 'success' })
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
<<<<<<< Updated upstream
  } catch (error: any) {
    toast.add({ 
      title: 'Erro ao alterar senha', 
      description: error.message || 'Verifique sua senha atual', 
      color: 'error' 
    })
=======
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao alterar senha'
    toast.add({ title: 'Erro', description: message, color: 'error' })
>>>>>>> Stashed changes
  } finally {
    savingPassword.value = false
  }
}

const deleteAccount = async () => {
  deletingAccount.value = true
  try {
    await del('/users/me')
    await supabase.auth.signOut()
    authStore.logout()
    toast.add({ title: 'Conta excluída com sucesso', color: 'success' })
    router.push('/')
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Erro ao excluir conta'
    toast.add({ title: 'Erro', description: message, color: 'error' })
  } finally {
    deletingAccount.value = false
  }
}

onMounted(async () => {
  try {
    studyProfile.value = await get<StudyProfile>('/onboarding')
  } catch (error) {
    console.error('Error loading study profile:', error)
  }
})
</script>