<script setup lang="ts">
import { computed } from 'vue'
import type { RecordData } from '@/api/platform'
import { extensionFields as fields } from '@/config/resources/extensions'
import FormField from '@/components/forms/FormField.vue'
import { recordLabel } from '@/utils/display'

const props = defineProps<{
  form: RecordData
  errors: Record<string, string>
  disabled?: boolean
  editing?: boolean
  targets: RecordData[]
  lookupLoading: boolean
  scope?: number
}>()
const options = computed(() => props.targets.map(target => ({
  value: Number(target.id),
  label: `${recordLabel(target)} · #${target.id}`,
})))
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
    v-model="form.extensionNumber"
    :field="fields.extensionNumber!"
    :error="errors.extensionNumber"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.targetType"
    :field="fields.targetType!"
    :error="errors.targetType"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.targetId"
    :field="fields.targetId!"
    :error="errors.targetId"
    :disabled="disabled || lookupLoading"
    :editing="editing"
    :options="options"
    :placeholder="lookupLoading ? 'Yükleniyor…' : 'Hedef seçin'"
  >
    <template #hint>
      <span v-if="!lookupLoading && !targets.length" class="form-hint">
        {{ scope ? 'Bu tenant’ta seçilebilir hedef yok. Önce ilgili kaydı oluşturun.' : 'Hedefleri görmek için tenant seçin.' }}
      </span>
    </template>
  </FormField>
  <FormField
    v-model="form.enabled"
    :field="fields.enabled!"
    :error="errors.enabled"
    :disabled="disabled"
    :editing="editing"
  />
</template>
