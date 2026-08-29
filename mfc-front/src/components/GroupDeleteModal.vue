<script setup>
import { toRef } from 'vue';
import { useEscapeClose } from '../composables/useEscapeClose';

const props = defineProps({
  show: Boolean,
  transaction: Object
});

const emit = defineEmits(['close', 'confirm']);
useEscapeClose(toRef(props, 'show'), () => emit('close'));
</script>

<template>
  <div
    v-if="show"
    role="dialog"
    aria-modal="true"
    aria-labelledby="group-delete-title"
    class="fixed inset-0 z-[200] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-md rounded-2xl border border-white bg-white p-6 shadow-xl dark:border-slate-700 dark:bg-slate-800">
      <h3 id="group-delete-title" class="text-lg font-bold text-slate-800 dark:text-slate-100">
        Excluir lançamento agrupado
      </h3>
      <p class="mt-2 text-sm text-slate-400">
        “{{ transaction?.description }}” pertence a um grupo. Escolha o alcance da exclusão.
      </p>

      <div class="mt-6 space-y-2">
        <button
          type="button"
          class="w-full rounded-xl border border-slate-200 px-4 py-3 text-left text-sm font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-700"
          @click="$emit('confirm', 'single')"
        >
          Somente este lançamento
        </button>
        <button
          type="button"
          class="w-full rounded-xl border border-amber-200 px-4 py-3 text-left text-sm font-semibold text-amber-700 hover:bg-amber-50 dark:border-amber-800 dark:text-amber-400 dark:hover:bg-amber-900/20"
          @click="$emit('confirm', 'forward')"
        >
          Este e os próximos do grupo
        </button>
        <button
          type="button"
          class="w-full rounded-xl bg-rose-500 px-4 py-3 text-left text-sm font-semibold text-white hover:bg-rose-600"
          @click="$emit('confirm', 'all')"
        >
          Todo o grupo
        </button>
      </div>

      <button type="button" class="mt-4 w-full py-2 text-sm font-medium text-slate-400" @click="$emit('close')">
        Cancelar
      </button>
    </div>
  </div>
</template>
