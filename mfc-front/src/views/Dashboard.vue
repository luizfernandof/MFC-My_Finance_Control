<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue';
import api from '../services/api';
import DoughnutChart from '../components/DoughnutChart.vue';
import { useBreakpoint } from '../composables/useBreakpoint';

const { isMobile } = useBreakpoint();
const now = new Date();
const selectedMonth = ref(now.getMonth() + 1);
const selectedYear = ref(now.getFullYear());
const loading = ref(false);
const chartKey = ref(0);

const summary = ref({ totalIncome: 0, totalExpense: 0, balance: 0 });
const expensesByCategory = ref([]);

const chartData = computed(() => {
  const dataRaw = expensesByCategory.value.map(c => ({
    name: c.category,
    value: Number(c.total) || 0
  }));

  const totalGeral = dataRaw.reduce((acc, curr) => acc + curr.value, 0);

  if (totalGeral === 0) {
    return { labels: [], datasets: [] };
  }

  const valuesWithDecimals = dataRaw.map(item => {
    const percentage = (item.value / totalGeral) * 100;
    return {
      ...item,
      percentage,
      integerPart: Math.floor(percentage),
      decimalPart: percentage - Math.floor(percentage)
    };
  });

  const sumIntegers = valuesWithDecimals.reduce((acc, curr) => acc + curr.integerPart, 0);
  let difference = 100 - sumIntegers;

  const sortedByDecimals = [...valuesWithDecimals].sort((a, b) => b.decimalPart - a.decimalPart);

  for (let i = 0; i < difference; i++) {
    sortedByDecimals[i].integerPart += 1;
  }

  return {
    labels: valuesWithDecimals.map(item => {
      const valorFormatado = item.value.toLocaleString('pt-BR', { minimumFractionDigits: 2 });
      return `${item.name}: R$ ${valorFormatado} (${item.integerPart}%)`;
    }),
    datasets: [{
      data: valuesWithDecimals.map(item => item.value),
      backgroundColor: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4'],
      borderWidth: 0,
      hoverOffset: 15
    }]
  };
});

async function fetchData() {
  loading.value = true;
  try {
    const params = { month: selectedMonth.value, year: selectedYear.value };
    const [resSummary, resExpenses] = await Promise.all([
      api.get('/dashboard/summary', { params }),
      api.get('/dashboard/expenses-by-category', { params })
    ]);

    summary.value = resSummary.data;
    expensesByCategory.value = resExpenses.data || [];

    await nextTick();
    chartKey.value++;
  } catch (error) {
    console.error("Erro ao carregar dashboard:", error);
  } finally {
    loading.value = false;
  }
}

watch([selectedMonth, selectedYear], fetchData);
onMounted(fetchData);

const months = [
  { value: 1, label: 'Janeiro' }, { value: 2, label: 'Fevereiro' },
  { value: 3, label: 'Março' }, { value: 4, label: 'Abril' },
  { value: 5, label: 'Maio' }, { value: 6, label: 'Junho' },
  { value: 7, label: 'Julho' }, { value: 8, label: 'Agosto' },
  { value: 9, label: 'Setembro' }, { value: 10, label: 'Outubro' },
  { value: 11, label: 'Novembro' }, { value: 12, label: 'Dezembro' }
];
const years = Array.from({ length: 6 }, (_, i) => now.getFullYear() - i);
</script>

<template>
  <div class="p-4 md:p-6 max-w-7xl mx-auto min-h-screen">

    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6 md:mb-8">
      <div class="w-full md:w-auto">
        <h2 class="text-2xl md:text-3xl font-bold text-slate-800 italic uppercase tracking-tighter leading-tight">
          Dashboard
        </h2>
        <p class="text-slate-400 text-xs font-medium mt-1">
          {{ months[selectedMonth - 1]?.label }} {{ selectedYear }}
        </p>
      </div>

      <div class="flex items-center gap-1 bg-white px-3 py-1.5 rounded-lg shadow-sm border border-slate-100 h-9">
        <select v-model="selectedMonth" class="bg-transparent text-sm font-medium outline-none w-20 text-center appearance-none cursor-pointer">
          <option v-for="m in months" :key="m.value" :value="m.value">{{ m.label }}</option>
        </select>
        <div class="w-px h-4 bg-slate-200"></div>
        <select v-model="selectedYear" class="bg-transparent text-sm font-medium outline-none text-center appearance-none w-14 cursor-pointer">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-4 mb-6 md:mb-8">
      <div class="bg-white p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col items-center">
        <span class="text-xs font-medium text-slate-400 uppercase tracking-wide mb-2">Entradas</span>
        <h3 class="text-2xl md:text-3xl font-bold text-emerald-500">R$ {{ (summary.totalIncome || 0).toFixed(2) }}</h3>
      </div>

      <div class="bg-white p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col items-center">
        <span class="text-xs font-medium text-slate-400 uppercase tracking-wide mb-2">Saídas</span>
        <h3 class="text-2xl md:text-3xl font-bold text-rose-500">R$ {{ (summary.totalExpense || 0).toFixed(2) }}</h3>
      </div>

      <div class="bg-white p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col items-center">
        <span class="text-xs font-medium text-slate-400 uppercase tracking-wide mb-2">Saldo</span>
        <h3 :class="summary.balance >= 0 ? 'text-blue-600' : 'text-rose-600'" class="text-2xl md:text-3xl font-bold">
          R$ {{ (summary.balance || 0).toFixed(2) }}
        </h3>
      </div>
    </div>

    <div class="bg-white p-5 md:p-8 rounded-xl shadow-sm border border-slate-100 flex flex-col items-center">
      <h3 class="text-xs font-medium text-slate-400 uppercase tracking-wide mb-6 text-center italic">
        Distribuição de Gastos
      </h3>

      <div class="w-full" :class="isMobile ? 'max-w-full' : 'max-w-xl'">
        <DoughnutChart
          v-if="expensesByCategory.length > 0"
          :key="chartKey"
          :chartData="chartData"
        />
        <div v-else-if="!loading" class="text-center py-12 text-slate-300 italic text-sm">
          Nenhum registro encontrado para este período.
        </div>
        <div v-else class="text-center py-12">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        </div>
      </div>
    </div>
  </div>
</template>
