<script setup lang="ts">
type ToastLike = {
  id: string
  open: boolean
  title?: string
  description?: string
  color?: string
  duration?: number
}

const { toasts, remove } = useToast()

// Evita criar múltiplos timers para o mesmo toast.
const timers = new Map<string, number>()

if (import.meta.client) {
  watch(
    toasts,
    (current) => {
      for (const toast of current as ToastLike[]) {
        if (!toast.open) continue
        if (timers.has(toast.id)) continue

        const ms = toast.duration ?? 3000
        const timer = window.setTimeout(() => {
          remove(toast.id)
          timers.delete(toast.id)
        }, ms)
        timers.set(toast.id, timer)
      }
    },
    { immediate: true, deep: true }
  )
}
</script>

<template>
  <div class="fixed right-4 top-4 z-50 flex w-[360px] max-w-[90vw] flex-col gap-2">
    <div
      v-for="toast in (toasts as ToastLike[])"
      :key="toast.id"
      v-show="toast.open"
      class="rounded-md border bg-neutral-900 px-4 py-3 text-neutral-50 shadow-md"
      :class="{
        'border-error': toast.color === 'error',
        'border-success': toast.color === 'success',
        'border-warning': toast.color === 'warning',
        'border-neutral-700': !toast.color || !['error', 'success', 'warning'].includes(toast.color),
      }"
    >
      <div v-if="toast.title" class="text-sm font-semibold">
        {{ toast.title }}
      </div>
      <div v-if="toast.description" class="mt-1 text-sm/5">
        {{ toast.description }}
      </div>
    </div>
  </div>
</template>

