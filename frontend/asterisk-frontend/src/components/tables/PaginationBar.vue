<script setup lang="ts">
defineProps<{ page: number; totalPages: number; visibleCount: number; loading?: boolean }>()
defineEmits<{ change: [offset: number] }>()
</script>

<template>
  <footer class="panel-footer">
    <span>{{ visibleCount }} kayıt gösteriliyor · Sayfa {{ page + 1 }} / {{ Math.max(1, totalPages) }}</span>
    <div class="actions">
      <button class="button button-small" :disabled="page === 0 || loading" @click="$emit('change', -1)">Önceki</button>
      <button class="button button-small" :disabled="page + 1 >= totalPages || loading" @click="$emit('change', 1)">Sonraki</button>
    </div>
  </footer>
</template>
