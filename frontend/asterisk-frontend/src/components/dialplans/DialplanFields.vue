<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { dialplanFields as fields } from '@/config/resources/dialplans'
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
    v-model="form.extension"
    :field="fields.extension!"
    :error="errors.extension"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.priority"
    :field="fields.priority!"
    :error="errors.priority"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.application"
    :field="fields.application!"
    :error="errors.application"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.applicationData"
    :field="fields.applicationData!"
    :error="errors.applicationData"
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
