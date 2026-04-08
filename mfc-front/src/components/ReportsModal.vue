<script setup>
import { ref, computed, nextTick } from 'vue';
import api from '../services/api';

const props = defineProps({
  show: Boolean
});

const emit = defineEmits(['close']);

const mes = ref(new Date().getMonth() + 1);
const ano = ref(new Date().getFullYear());
const isLoading = ref(false);
const apiError = ref('');

const meses = [
  { value: 1, label: 'Janeiro' },
  { value: 2, label: 'Fevereiro' },
  { value: 3, label: 'Março' },
  { value: 4, label: 'Abril' },
  { value: 5, label: 'Maio' },
  { value: 6, label: 'Junho' },
  { value: 7, label: 'Julho' },
  { value: 8, label: 'Agosto' },
  { value: 9, label: 'Setembro' },
  { value: 10, label: 'Outubro' },
  { value: 11, label: 'Novembro' },
  { value: 12, label: 'Dezembro' }
];

const anos = computed(() => {
  const currentYear = new Date().getFullYear();
  return [currentYear, currentYear - 1, currentYear - 2];
});

async function generateReport() {
  isLoading.value = true;
  apiError.value = '';

  try {
    const response = await api.get(`/reports/transactions/monthly?month=${mes.value}&year=${ano.value}`, {
      responseType: 'blob'
    });

    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;

    const mesLabel = meses.find(m => m.value === Number(mes.value))?.label || mes.value;
    link.download = `transacoes_${mesLabel.toLowerCase()}_${ano.value}.pdf`;

    document.body.appendChild(link);
    link.click();

    await nextTick();

    window.URL.revokeObjectURL(url);
    emit('close');
  } catch (error) {
    isLoading.value = false;
    if (error.response?.data?.type === 'application/json') {
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const jsonError = JSON.parse(reader.result);
          apiError.value = jsonError.message || 'Erro ao gerar relatório';
        } catch {
          apiError.value = 'Erro ao gerar relatório';
        }
      };
      reader.readAsText(error.response.data);
    } else {
      apiError.value = 'Erro ao conectar com o servidor';
    }
  }
}
</script>

<template>
  <div v-if="show" class="fixed inset-0 z-[100] flex items-end md:items-center justify-center p-0 md:p-4 bg-slate-900/60 backdrop-blur-sm">
    <div class="bg-white rounded-t-[2rem] md:rounded-[2.5rem] shadow-2xl w-full max-w-lg p-6 md:p-10 border border-white">

      <div class="flex justify-between items-center mb-6 md:mb-8">
        <h2 class="text-xl md:text-2xl font-black text-slate-800 italic tracking-tight">
          Gerar Relatório PDF
        </h2>
        <button @click="$emit('close')" class="text-slate-300 hover:text-slate-500 p-2">
          <font-awesome-icon icon="fa-solid fa-xmark" class="text-2xl" />
        </button>
      </div>

      <div v-if="apiError" class="mb-6 p-4 bg-rose-50 text-rose-600 text-[10px] font-black rounded-2xl border border-rose-100 uppercase text-center tracking-widest">
        <i class="fa-solid fa-triangle-exclamation mr-2"></i>
        {{ apiError }}
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Mês</label>
          <div class="relative">
            <select v-model="mes"
              class="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-bold text-slate-700 appearance-none">
              <option v-for="m in meses" :key="m.value" :value="m.value">
                {{ m.label }}
              </option>
            </select>
            <font-awesome-icon icon="fa-solid fa-chevron-down" class="absolute right-5 top-1/2 -translate-y-1/2 text-slate-300 pointer-events-none" />
          </div>
        </div>

        <div>
          <label class="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Ano</label>
          <div class="relative">
            <select v-model="ano"
              class="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-bold text-slate-700 appearance-none">
              <option v-for="a in anos" :key="a" :value="a">
                {{ a }}
              </option>
            </select>
            <font-awesome-icon icon="fa-solid fa-chevron-down" class="absolute right-5 top-1/2 -translate-y-1/2 text-slate-300 pointer-events-none" />
          </div>
        </div>
      </div>

      <div class="flex flex-col md:flex-row gap-2 md:gap-4 pt-6">
        <button type="button" @click="$emit('close')"
          class="order-2 md:order-1 flex-1 py-4 text-slate-400 font-black uppercase text-[10px] tracking-widest flex items-center justify-center gap-2 hover:bg-slate-50 rounded-2xl transition-all">
          Cancelar
        </button>
        <button @click="generateReport" :disabled="isLoading"
          class="order-1 md:order-2 flex-1 bg-blue-600 text-white py-4 rounded-2xl font-black shadow-lg shadow-blue-200 hover:bg-blue-700 transition-all transform active:scale-95 uppercase text-[10px] tracking-widest flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
          <font-awesome-icon v-if="isLoading" icon="fa-solid fa-spinner" class="animate-spin" />
          <font-awesome-icon v-else icon="fa-solid fa-file-pdf" />
          {{ isLoading ? 'Gerando...' : 'Baixar PDF' }}
        </button>
      </div>
    </div>
  </div>
</template>
