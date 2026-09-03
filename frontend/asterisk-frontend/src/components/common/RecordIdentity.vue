<script setup lang="ts">
import AppIcon from './AppIcon.vue'
defineProps<{ name: unknown; id: unknown; icon: string }>()
</script>

<template>
  <div class="record-name">
    <span class="record-avatar"><AppIcon :name="icon" :size="16" /></span>
    <div><strong>{{ name }}</strong><small>ID #{{ id }}</small></div>
  </div>
</template>
