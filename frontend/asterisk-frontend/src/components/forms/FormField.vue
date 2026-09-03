<script setup lang="ts">
import { computed } from 'vue'
import { displayValue } from '@/utils/display'
import type { Field } from '@/types/resource'
type Value = string | number | boolean | null | undefined
const props = defineProps<{
  field: Field
  disabled?: boolean
  editing?: boolean
  error?: string
  options?: { value: string | number; label: string }[]
  placeholder?: string
}>()
const model = defineModel<Value>()
const required = computed(() => Boolean(props.field.required && !(props.editing && props.field.type === 'password')))
const id = computed(() => `field-${props.field.key}`)
</script>

<template>
  <label :for="id" :class="{ full: field.type === 'textarea', checkbox: field.type === 'checkbox' }">
    <template v-if="field.type === 'checkbox'">
      <input :id="id" v-model="model" type="checkbox" :disabled="disabled" />{{ field.label }}
    </template>
    <template v-else>
      {{ field.label }} {{ required ? '*' : '' }}
      <select v-if="field.type === 'select'" :id="id" v-model="model" :required="required" :disabled="disabled" :aria-invalid="Boolean(error)">
        <template v-if="options">
          <option value="" disabled>{{ placeholder || 'Hedef seçin' }}</option>
          <option v-for="option in options" :key="option.value" :value="option.value">{{ option.label }}</option>
        </template>
        <option v-for="option in field.options" :key="option" :value="option">{{ displayValue(option) }}</option>
      </select>
      <textarea v-else-if="field.type === 'textarea'" :id="id" :value="String(model || '')" :maxlength="field.maxLength" :required="required" :disabled="disabled" @input="model = ($event.target as HTMLTextAreaElement).value" />
      <input v-else :id="id" v-model="model" :type="field.type || 'text'" :required="required" :min="field.min" :max="field.max" :minlength="field.minLength" :maxlength="field.maxLength" :pattern="field.pattern" :disabled="disabled" :autocomplete="field.type === 'password' ? 'new-password' : 'off'" :aria-invalid="Boolean(error)" :aria-describedby="error || field.hint ? `${id}-hint` : undefined" />
    </template>
    <span v-if="error" :id="`${id}-hint`" class="field-error">{{ error }}</span>
    <span v-else-if="field.hint" :id="`${id}-hint`" class="form-hint">{{ field.hint }}</span>
    <slot name="hint" />
  </label>
</template>
