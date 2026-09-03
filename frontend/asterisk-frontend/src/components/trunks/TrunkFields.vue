<script setup lang="ts">
import type { RecordData } from '@/api/platform'
import { trunkFields as fields } from '@/config/resources/trunks'
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
    v-model="form.host"
    :field="fields.host!"
    :error="errors.host"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.port"
    :field="fields.port!"
    :error="errors.port"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.username"
    :field="fields.username!"
    :error="errors.username"
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
    v-model="form.transport"
    :field="fields.transport!"
    :error="errors.transport"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.fromUser"
    :field="fields.fromUser!"
    :error="errors.fromUser"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.fromDomain"
    :field="fields.fromDomain!"
    :error="errors.fromDomain"
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
