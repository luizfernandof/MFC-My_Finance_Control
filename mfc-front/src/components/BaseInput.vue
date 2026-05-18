<script setup>
defineProps({
  label: String,
  modelValue: [String, Number],
  type: { type: String, default: 'text' },
  placeholder: String,
  error: String,
  required: Boolean
});

defineEmits(['update:modelValue']);
</script>

<template>
  <div class="w-full">
    <label v-if="label" class="block text-xs font-semibold text-slate-500 dark:text-slate-400 mb-2 ml-1">
      {{ label }}
    </label>

    <div class="relative">
      <input
        :type="type"
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :placeholder="placeholder"
        :class="[
          error
            ? 'border-rose-400 bg-rose-50 dark:bg-rose-900/20 focus:ring-rose-500 text-rose-900 dark:text-rose-300'
            : 'border-slate-200 bg-slate-50 dark:bg-slate-700 dark:border-slate-600 focus:ring-blue-500 text-slate-700 dark:text-slate-200',
          'w-full px-4 py-3 border rounded-xl focus:ring-2 focus:bg-white dark:focus:bg-slate-600 outline-none transition-all placeholder:text-slate-300 dark:placeholder:text-slate-500',
          $slots.suffix ? 'pr-10' : ''
        ]"
      />

      <div v-if="$slots.suffix" class="absolute inset-y-0 right-0 flex items-center pr-3">
        <slot name="suffix" />
      </div>
    </div>

    <transition name="fade">
      <p v-if="error" class="text-rose-500 dark:text-rose-400 text-xs font-medium mt-1 ml-1 italic">
        {{ error }}
      </p>
    </transition>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>