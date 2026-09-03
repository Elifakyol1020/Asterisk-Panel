<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { queueFields as fields } from '@/config/resources/queues'
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
    v-model="form.strategy"
    :field="fields.strategy!"
    :error="errors.strategy"
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
    v-model="form.retry"
    :field="fields.retry!"
    :error="errors.retry"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.wrapupTime"
    :field="fields.wrapupTime!"
    :error="errors.wrapupTime"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.maxLength"
    :field="fields.maxLength!"
    :error="errors.maxLength"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.musicOnHold"
    :field="fields.musicOnHold!"
    :error="errors.musicOnHold"
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
