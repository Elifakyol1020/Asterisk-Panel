<script setup lang="ts">
import { watch } from 'vue'
import type { RecordData } from '@/api/platform'
import { tenantFields as fields } from '@/config/resources/tenants'
import FormField from '@/components/forms/FormField.vue'
import { normalizeTenantCode } from '@/utils/resourceForm'

const props = defineProps<{
  form: RecordData
  errors: Record<string, string>
  disabled?: boolean
  editing?: boolean
}>()

let lastGenerated = ''

watch(() => props.form.name, value => {
  const generated = normalizeTenantCode(value)
  if (!props.editing && generated && (!props.form.code || props.form.code === lastGenerated)) {
    props.form.code = generated
    lastGenerated = generated
  }
})

watch(() => props.form.code, value => {
  const normalized = normalizeTenantCode(value)
  if (typeof value === 'string' && value !== normalized) props.form.code = normalized
})
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
    v-model="form.code"
    :field="fields.code!"
    :error="errors.code"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.status"
    :field="fields.status!"
    :error="errors.status"
    :disabled="disabled"
    :editing="editing"
  />
</template>
