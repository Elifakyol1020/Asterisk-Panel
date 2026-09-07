<script setup lang="ts">
import { useTheme, type Theme } from '@/composables/useTheme'
const { preference, setTheme } = useTheme()
const themes: { value: Theme; label: string; icon: string }[] = [
  { value: 'light', label: 'Açık', icon: 'M12 3v2m0 14v2M3 12h2m14 0h2M5.6 5.6 7 7m10 10 1.4 1.4M5.6 18.4 7 17M17 7l1.4-1.4M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0' },
  { value: 'dark', label: 'Koyu', icon: 'M20.5 13a8.5 8.5 0 0 1-9.5-9.5A8.5 8.5 0 1 0 20.5 13Z' },
  { value: 'system', label: 'Sistem', icon: 'M4 4h16v12H4zM8 20h8m-4-4v4' },
]
</script>
<template>
  <div class="theme-picker" role="group" aria-label="Görünüm teması">
    <button v-for="theme in themes" :key="theme.value" type="button" :aria-label="theme.label + ' tema'" :title="theme.label + ' tema'" :aria-pressed="preference === theme.value" @click="setTheme(theme.value)">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path :d="theme.icon" /></svg>
      <span>{{ theme.label }}</span>
    </button>
  </div>
</template>
