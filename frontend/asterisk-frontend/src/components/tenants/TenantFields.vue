<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { tenantFields as fields } from '@/config/resources/tenants'
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
