<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { ivrFields as fields } from '@/config/resources/ivrs'
import FormField from '@/components/forms/FormField.vue'

const props = defineProps<{
  form: RecordData
  errors: Record<string, string>
  disabled?: boolean
  editing?: boolean
}>()
</script>

<template>
  <FormField
    v-model="form.name"
    :field="fields.name!"
    :error="errors.name"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.audioFile"
    :field="fields.audioFile!"
    :error="errors.audioFile"
    :disabled="disabled"
    :editing="editing"
  />
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
