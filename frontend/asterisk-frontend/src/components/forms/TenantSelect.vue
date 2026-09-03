<script setup lang="ts">
import type { RecordData } from '@/api/platform'
withDefaults(defineProps<{ tenants: RecordData[]; disabled?: boolean; required?: boolean; placeholder?: string }>(), {
  placeholder: 'Tenant seçin',
})
const model = defineModel<string>({ required: true })
</script>

<template>
  <select v-model="model" aria-label="Tenant" :required="required" :disabled="disabled">
    <option value="" :disabled="required">{{ placeholder }}</option>
    <option v-for="tenant in tenants" :key="String(tenant.id)" :value="String(tenant.id)">
      {{ tenant.name }} · #{{ tenant.id }}{{ tenant.status === 'INACTIVE' ? ' (Pasif)' : '' }}
    </option>
  </select>
</template>
