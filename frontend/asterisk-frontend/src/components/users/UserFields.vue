<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { userFields as fields } from '@/config/resources/users'
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
    v-model="form.username"
    :field="fields.username!"
    :error="errors.username"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.email"
    :field="fields.email!"
    :error="errors.email"
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
