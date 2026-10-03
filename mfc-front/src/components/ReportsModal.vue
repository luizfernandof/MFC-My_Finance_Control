<script setup>
import { computed, ref, nextTick, toRef, watch } from 'vue';
import api from '../services/api';
import { months as meses, years as anos } from '../composables/usePeriodOptions';
import { useEscapeClose } from '../composables/useEscapeClose';
import { getBlobApiErrorMessage } from '../utils/apiError';

const props = defineProps({
  show: Boolean,
  reportType: {
    type: String,
    default: 'transactions'
  }
});

const emit = defineEmits(['close']);
useEscapeClose(toRef(props, 'show'), () => emit('close'));

const mes = ref(new Date().getMonth() + 1);
const ano = ref(new Date().getFullYear());
const isLoading = ref(false);
const apiError = ref('');

const reportConfig = computed(() => props.reportType === 'categories'
  ? {
      title: 'Gastos por Categoria',
      description: 'Participação, comparação mensal e maiores despesas.',
      endpoint: '/reports/categories/monthly',
      filename: 'gastos_por_categoria'
    }
  : {
      title: 'Extrato Mensal',
      description: 'Resumo financeiro e todos os lançamentos do período.',
      endpoint: '/reports/transactions/monthly',
      filename: 'transacoes'
    });

watch([() => props.show, () => props.reportType], () => {
  apiError.value = '';
});

async function generateReport() {
  isLoading.value = true;
  apiError.value = '';

  try {
    const response = await api.get(reportConfig.value.endpoint, {
      params: { month: mes.value, year: ano.value },
      responseType: 'blob'
    });

    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;

    const mesLabel = meses.find(m => m.value === Number(mes.value))?.label || mes.value;
    link.download = `${reportConfig.value.filename}_${mesLabel.toLowerCase()}_${ano.value}.pdf`;

    document.body.appendChild(link);
    link.click();
    link.remove();

    await nextTick();

    window.URL.revokeObjectURL(url);
    emit('close');
  } catch (error) {
    apiError.value = await getBlobApiErrorMessage(error, 'Não foi possível gerar o relatório.');
  } finally {
    isLoading.value = false;
  }
}
</script>

<template>
  <div v-if="show" role="dialog" aria-modal="true" aria-labelledby="report-title" class="fixed inset-0 z-[100] flex items-end md:items-center justify-center p-0 md:p-4 bg-slate-900/60 backdrop-blur-sm">
    <div class="bg-white dark:bg-slate-800 rounded-t-[2rem] md:rounded-[2.5rem] shadow-2xl w-full max-w-lg p-6 md:p-10 border border-white dark:border-slate-700">

      <div class="flex justify-between items-center mb-4">
        <div>
          <h2 id="report-title" class="text-xl md:text-2xl font-black text-slate-800 dark:text-slate-100 italic tracking-tight">
            {{ reportConfig.title }}
          </h2>
          <p class="mt-1 text-xs font-medium text-slate-400 dark:text-slate-500">
            {{ reportConfig.description }}
          </p>
        </div>
        <button type="button" aria-label="Fechar relatório" @click="$emit('close')" class="text-slate-300 hover:text-slate-500 dark:text-slate-500 dark:hover:text-slate-300 p-2">
          <font-awesome-icon icon="fa-solid fa-xmark" class="text-2xl" />
        </button>
      </div>

      <div v-if="apiError" class="mb-4 p-4 bg-rose-50 dark:bg-rose-900/20 text-rose-600 dark:text-rose-400 text-[10px] font-black rounded-2xl border border-rose-100 dark:border-rose-800 uppercase text-center tracking-widest">
        <i class="fa-solid fa-triangle-exclamation mr-2"></i>
        {{ apiError }}
      </div>

      <div class="space-y-3">
        <div>
          <label for="report-month" class="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Mês</label>
          <div class="relative">
            <select id="report-month" v-model="mes"
              class="w-full px-5 py-4 bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-bold text-slate-700 dark:text-slate-200 appearance-none">
              <option v-for="m in meses" :key="m.value" :value="m.value">
                {{ m.label }}
              </option>
            </select>
            <font-awesome-icon icon="fa-solid fa-chevron-down" class="absolute right-5 top-1/2 -translate-y-1/2 text-slate-300 dark:text-slate-500 pointer-events-none" />
          </div>
        </div>

        <div>
          <label for="report-year" class="block text-[10px] font-black text-slate-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Ano</label>
          <div class="relative">
            <select id="report-year" v-model="ano"
              class="w-full px-5 py-4 bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-bold text-slate-700 dark:text-slate-200 appearance-none">
              <option v-for="a in anos" :key="a" :value="a">
                {{ a }}
              </option>
            </select>
            <font-awesome-icon icon="fa-solid fa-chevron-down" class="absolute right-5 top-1/2 -translate-y-1/2 text-slate-300 dark:text-slate-500 pointer-events-none" />
          </div>
        </div>
      </div>

      <div class="flex flex-col md:flex-row gap-2 pt-4">
        <button type="button" @click="$emit('close')"
          class="order-2 md:order-1 flex-1 py-4 text-slate-400 dark:text-slate-500 font-black uppercase text-[10px] tracking-widest flex items-center justify-center gap-2 hover:bg-slate-50 dark:hover:bg-slate-700 rounded-2xl transition-all">
          Cancelar
        </button>
        <button @click="generateReport" :disabled="isLoading"
          class="order-1 md:order-2 flex-1 bg-blue-600 dark:bg-blue-500 text-white py-4 rounded-2xl font-black shadow-lg shadow-blue-200 dark:shadow-blue-900/30 hover:bg-blue-700 dark:hover:bg-blue-600 transition-all transform active:scale-95 uppercase text-[10px] tracking-widest flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
          <font-awesome-icon v-if="isLoading" icon="fa-solid fa-spinner" class="animate-spin" />
          <font-awesome-icon v-else icon="fa-solid fa-file-pdf" />
          {{ isLoading ? 'Gerando...' : 'Baixar PDF' }}
        </button>
      </div>
    </div>
  </div>
</template>
