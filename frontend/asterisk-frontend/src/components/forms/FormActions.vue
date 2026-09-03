<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
defineProps<{ cancelTo: RouteLocationRaw; saving?: boolean; disabled?: boolean }>()
</script>

<template>
  <div class="form-actions">
    <RouterLink :to="cancelTo" class="button">Vazgeç</RouterLink>
    <button class="button button-primary" :disabled="saving || disabled">
      <AppIcon name="check" :size="16" />{{ saving ? 'Kaydediliyor…' : 'Değişiklikleri kaydet' }}
    </button>
  </div>
</template>
