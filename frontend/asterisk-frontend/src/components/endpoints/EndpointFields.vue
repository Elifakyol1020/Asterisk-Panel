<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { endpointFields as fields } from '@/config/resources/endpoints'
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
    v-model="form.displayName"
    :field="fields.displayName!"
    :error="errors.displayName"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.extension"
    :field="fields.extension!"
    :error="errors.extension"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.transport"
    :field="fields.transport!"
    :error="errors.transport"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.codecs"
    :field="fields.codecs!"
    :error="errors.codecs"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.password"
    :field="fields.password!"
    :error="errors.password"
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
