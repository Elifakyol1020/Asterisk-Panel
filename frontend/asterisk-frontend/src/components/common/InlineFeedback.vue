<script setup lang="ts">
import AppIcon from './AppIcon.vue'
defineProps<{ error?: string; success?: string; retry?: boolean }>()
defineEmits<{ retry: [] }>()
</script>

<template>
  <div v-if="error" class="form-error" role="alert">
    {{ error }}
    <button v-if="retry" type="button" class="button button-small" @click="$emit('retry')">Tekrar dene</button>
  </div>
  <div v-if="success" class="notice success feedback-success" role="status">
    <AppIcon name="check" :size="18" />{{ success }}
  </div>
</template>

<style scoped>
.feedback-success { margin-bottom: 18px; }
</style>
