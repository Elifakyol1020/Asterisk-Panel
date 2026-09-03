<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import AppIcon from './AppIcon.vue'

const props = defineProps<{ open: boolean; title: string; confirmLabel: string; busy?: boolean }>()
const emit = defineEmits<{ confirm: []; cancel: [] }>()
const dialog = ref<HTMLDialogElement>()
function sync() {
  if (props.open && !dialog.value?.open) dialog.value?.showModal()
  if (!props.open && dialog.value?.open) dialog.value?.close()
}
function cancel(event: Event) {
  event.preventDefault()
  if (!props.busy) emit('cancel')
}
watch(() => props.open, sync)
onMounted(sync)
</script>

<template>
  <dialog ref="dialog" class="modal" aria-labelledby="confirmation-title" @cancel="cancel">
    <AppIcon name="trash" :size="25" />
    <h2 id="confirmation-title">{{ title }}</h2>
    <p><slot /></p>
    <div class="modal-actions">
      <button class="button" :disabled="busy" @click="emit('cancel')">Vazgeç</button>
      <button class="button button-danger" :disabled="busy" @click="emit('confirm')">
        {{ busy ? 'İşleniyor…' : confirmLabel }}
      </button>
    </div>
  </dialog>
</template>
