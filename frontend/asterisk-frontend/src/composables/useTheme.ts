import { ref } from 'vue'
export type Theme = 'light' | 'dark' | 'system'
const key = 'netgsm-theme'
const preference = ref<Theme>('system')
const media = window.matchMedia('(prefers-color-scheme: dark)')
function valid(value: unknown): value is Theme { return value === 'light' || value === 'dark' || value === 'system' }
function apply() {
  const dark = preference.value === 'dark' || (preference.value === 'system' && media.matches)
  document.documentElement.dataset.theme = dark ? 'dark' : 'light'
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', dark ? '#101720' : '#f4f6f9')
}
try { const saved = localStorage.getItem(key); if (valid(saved)) preference.value = saved } catch { /* Storage may be disabled. */ }
apply()
media.addEventListener('change', apply)
window.addEventListener('storage', event => {
  if (event.key === key || event.key === null) { preference.value = valid(event.newValue) ? event.newValue : 'system'; apply() }
})
export function useTheme() {
  function setTheme(value: Theme) {
    if (!valid(value)) return
    preference.value = value
    try { localStorage.setItem(key, value) } catch { /* The theme still works for this session. */ }
    apply()
  }
  return { preference, setTheme }
}
