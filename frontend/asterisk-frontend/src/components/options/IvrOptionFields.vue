<script setup lang="ts">
import { computed } from 'vue'
import type { RecordData } from '@/api/platform'
import { ivrOptionFields as fields } from '@/config/resources/options'
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
    v-model="form.digit"
    :field="fields.digit!"
    :error="errors.digit"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.actionType"
    :field="fields.actionType!"
    :error="errors.actionType"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField v-if="form.actionType !== 'HANGUP'"
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
</template>
