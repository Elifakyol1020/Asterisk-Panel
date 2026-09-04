<script setup lang="ts">
import { ref } from 'vue'
import type { RecordData } from '@/api/platform'
import { errorMessage } from '@/api/platform'
import api from '@/api/axios'
import { ivrFields as fields } from '@/config/resources/ivrs'
import FormField from '@/components/forms/FormField.vue'

const props = defineProps<{
  form: RecordData
  errors: Record<string, string>
  disabled?: boolean
  editing?: boolean
  tenantId?: number
}>()
const uploading = ref(false)
const uploadError = ref('')

async function uploadAudio(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!props.tenantId) { uploadError.value = 'Önce tenant seçin.'; input.value = ''; return }
  uploading.value = true
  uploadError.value = ''
  try {
    const data = new FormData()
    data.append('file', file)
    const response = await api.post<{ audioFile: string }>('/ivrs/audio', data, {
      params: { tenantId: props.tenantId },
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    props.form.audioFile = response.data.audioFile
  } catch (cause) {
    uploadError.value = errorMessage(cause)
  } finally {
    uploading.value = false
    input.value = ''
  }
}
</script>

<template>
  <FormField
    v-model="form.name"
    :field="fields.name!"
    :error="errors.name"
    :disabled="disabled"
    :editing="editing"
  />
  <label class="full">
    Karşılama ses dosyası *
    <input v-model="form.audioFile" type="hidden" />
    <input type="file" accept=".wav,audio/wav,audio/x-wav" :disabled="disabled || uploading || !tenantId" @change="uploadAudio" />
    <span v-if="uploading" class="form-hint">Ses dosyası yükleniyor…</span>
    <span v-else-if="uploadError" class="field-error">{{ uploadError }}</span>
    <span v-else-if="errors.audioFile" class="field-error">{{ errors.audioFile }}</span>
    <span v-else-if="form.audioFile" class="form-hint">Yüklendi: {{ form.audioFile }}</span>
    <span v-else class="form-hint">En fazla 20 MB WAV. Asterisk ile ortak ses volume’ünde tenant’a özel saklanır.</span>
  </label>
  <FormField
    v-model="form.timeout"
    :field="fields.timeout!"
    :error="errors.timeout"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.maxAttempts"
    :field="fields.maxAttempts!"
    :error="errors.maxAttempts"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.description"
    :field="fields.description!"
    :error="errors.description"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.enabled"
    :field="fields.enabled!"
    :error="errors.enabled"
    :disabled="disabled"
    :editing="editing"
  />
</template>
