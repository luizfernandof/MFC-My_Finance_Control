<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalElements: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  pageSizeOptions: { type: Array, default: () => [5, 10, 15, 30, 50] }
});

const emit = defineEmits(['update:currentPage', 'update:pageSize']);

const firstItem = computed(() => props.totalElements === 0 ? 0 : (props.currentPage * props.pageSize) + 1);
const lastItem = computed(() => Math.min((props.currentPage + 1) * props.pageSize, props.totalElements));

function changePage(page) {
  if (page >= 0 && page < props.totalPages && page !== props.currentPage) {
    emit('update:currentPage', page);
  }
}

function changePageSize(event) {
  emit('update:pageSize', Number(event.target.value));
}
</script>

<template>
  <nav aria-label="Paginação" class="flex flex-col sm:flex-row items-center justify-between gap-3 py-4">
    <p class="text-xs font-medium text-slate-400 dark:text-slate-500">
      Exibindo {{ firstItem }}–{{ lastItem }} de {{ totalElements }} registros
    </p>

    <div class="flex flex-wrap items-center justify-center gap-2">
      <label class="flex items-center gap-2 text-[10px] font-semibold uppercase tracking-wide text-slate-400 dark:text-slate-500">
        Exibir
        <select
          :value="pageSize"
          aria-label="Registros por página"
          @change="changePageSize"
          class="h-9 rounded-lg border border-slate-200 bg-white px-2 text-sm font-semibold text-blue-600 outline-none focus:ring-2 focus:ring-blue-500 dark:border-slate-700 dark:bg-slate-800 dark:text-blue-400"
        >
          <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
        </select>
      </label>

      <div class="flex h-9 items-center rounded-lg border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800">
        <button
          type="button"
          aria-label="Página anterior"
          :disabled="currentPage === 0 || totalPages === 0"
          @click="changePage(currentPage - 1)"
          class="h-full px-3 text-blue-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-300 dark:text-blue-400 dark:hover:bg-slate-700 dark:disabled:text-slate-600"
        >
          <font-awesome-icon icon="fa-solid fa-chevron-left" />
        </button>
        <span class="min-w-20 px-2 text-center text-xs font-semibold text-slate-500 dark:text-slate-400">
          {{ totalPages ? currentPage + 1 : 0 }} de {{ totalPages }}
        </span>
        <button
          type="button"
          aria-label="Próxima página"
          :disabled="currentPage >= totalPages - 1 || totalPages === 0"
          @click="changePage(currentPage + 1)"
          class="h-full px-3 text-blue-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-300 dark:text-blue-400 dark:hover:bg-slate-700 dark:disabled:text-slate-600"
        >
          <font-awesome-icon icon="fa-solid fa-chevron-right" />
        </button>
      </div>
    </div>
  </nav>
</template>
