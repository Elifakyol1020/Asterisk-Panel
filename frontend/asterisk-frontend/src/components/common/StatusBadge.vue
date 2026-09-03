<script setup lang="ts">
import { displayValue } from '@/utils/display'
defineProps<{ value: unknown; field?: string }>()
</script>

<template>
  <span class="badge" :class="{ inactive: value === false || value === 'INACTIVE' }">
    {{ displayValue(value, field) }}
  </span>
</template>
