<script setup>
defineProps({
  label: String,
  modelValue: [String, Number],
  type: { type: String, default: 'text' },
  placeholder: String,
  error: String, // Recebe a mensagem de erro do pai
  required: Boolean
});

defineEmits(['update:modelValue']);
</script>

<template>
  <div class="w-full">
    <label v-if="label" class="block text-xs font-semibold text-slate-500 mb-2 ml-1">
      {{ label }}
    </label>

    <input
      :type="type"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :placeholder="placeholder"
      :class="[
        error
          ? 'border-rose-400 bg-rose-50 focus:ring-rose-500 text-rose-900'
          : 'border-slate-200 bg-slate-50 focus:ring-blue-500 text-slate-700',
        'w-full px-4 py-3 border rounded-xl focus:ring-2 focus:bg-white outline-none transition-all placeholder:text-slate-300'
      ]"
    />

    <transition name="fade">
      <p v-if="error" class="text-rose-500 text-xs font-medium mt-1 ml-1 italic">
        {{ error }}
      </p>
    </transition>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>