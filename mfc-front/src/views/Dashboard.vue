<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue';
import api from '../services/api';
import DoughnutChart from '../components/DoughnutChart.vue';
import BarChart from '../components/BarChart.vue';
import { useBreakpoint } from '../composables/useBreakpoint';
import { useTheme } from '../composables/useTheme';

const { isMobile } = useBreakpoint();
const { isDark } = useTheme();
const now = new Date();
const selectedMonth = ref(now.getMonth() + 1);
const selectedYear = ref(now.getFullYear());
const loading = ref(false);
const chartKey = ref(0);
const barChartKey = ref(0);
const exporting = ref(false);

const summary = ref({ totalIncome: 0, totalExpense: 0, balance: 0, previousIncome: 0, previousExpense: 0, previousBalance: 0, expenseByCategory: [] });
const trendData = ref([]);
const recentTransactions = ref([]);

function calcChange(current, previous) {
  const c = Number(current) || 0;
  const p = Number(previous) || 0;
  if (p === 0 && c === 0) return null;
  if (p === 0) return null;
  return ((c - p) / p) * 100;
}

function formatChange(value) {
  if (value === null) return '';
  const sign = value > 0 ? '+' : '';
  return sign + value.toFixed(1) + '%';
}

const topCategories = computed(() => {
  return [...summary.value.expenseByCategory]
    .sort((a, b) => Number(b.total) - Number(a.total))
    .slice(0, 3);
});

const chartData = computed(() => {
  const data = summary.value.expenseByCategory || [];
  const dataRaw = data.map(c => ({
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

const chartOptions = computed(() => {
  const isSmallScreen = window.innerWidth < 768;

  return {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '72%',
    plugins: {
      legend: {
        position: 'bottom',
        align: isSmallScreen ? 'start' : 'center',
        labels: { 
          padding: 20, 
          usePointStyle: true,
          pointStyle: 'rectRounded',
          boxWidth: 8,
          textAlign: 'left',
          font: { 
            weight: 'bold', 
            family: 'Inter, sans-serif',
            size: isSmallScreen ? 11 : 13
          },
          color: isDark.value ? '#94a3b8' : '#475569'
        }
      },
      tooltip: {
        backgroundColor: isDark.value ? '#0f172a' : '#1e293b',
        padding: 12,
        cornerRadius: 10
      }
    },
    layout: {
      padding: isSmallScreen ? { left: 20, right: 20, bottom: 20 } : 0
    }
  };
});

const trendChartData = computed(() => {
  if (!trendData.value.length) return { labels: [], datasets: [] };

  const shortMonths = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  return {
    labels: trendData.value.map(t => shortMonths[t.month - 1] + '/' + t.year),
    datasets: [
      {
        label: 'Entradas',
        data: trendData.value.map(t => Number(t.totalIncome) || 0),
        backgroundColor: '#10b981',
        borderRadius: 6,
        maxBarThickness: 32
      },
      {
        label: 'Saídas',
        data: trendData.value.map(t => Number(t.totalExpense) || 0),
        backgroundColor: '#ef4444',
        borderRadius: 6,
        maxBarThickness: 32
      }
    ]
  };
});

const trendChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
      labels: {
        usePointStyle: true,
        pointStyle: 'rectRounded',
        boxWidth: 8,
        font: { weight: 'bold', family: 'Inter, sans-serif', size: 12 },
        color: isDark.value ? '#94a3b8' : '#475569'
      }
    },
    tooltip: {
      backgroundColor: isDark.value ? '#0f172a' : '#1e293b',
      padding: 12,
      cornerRadius: 10,
      callbacks: {
        label: function(context) {
          const val = context.parsed.y.toLocaleString('pt-BR', { minimumFractionDigits: 2 });
          return context.dataset.label + ': R$ ' + val;
        }
      }
    }
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { color: isDark.value ? '#94a3b8' : '#475569', font: { family: 'Inter, sans-serif', size: 11 } }
    },
    y: {
      beginAtZero: true,
      grid: { color: isDark.value ? '#1e293b' : '#f1f5f9' },
      ticks: {
        color: isDark.value ? '#94a3b8' : '#475569',
        font: { family: 'Inter, sans-serif', size: 11 },
        callback: function(value) {
          if (value >= 1000) return 'R$ ' + (value / 1000).toFixed(1) + 'k';
          return 'R$ ' + value;
        }
      }
    }
  }
}));

async function fetchData() {
  loading.value = true;
  try {
    const params = { month: selectedMonth.value, year: selectedYear.value };
    const [resSummary, resTrend, resTransactions] = await Promise.all([
      api.get('/dashboard/summary', { params }),
      api.get('/dashboard/monthly-trend', { params }),
      api.get('/transactions', { params: { ...params, page: 0, size: 5, sort: 'date,desc' } })
    ]);

    summary.value = resSummary.data;
    trendData.value = resTrend.data || [];
    recentTransactions.value = resTransactions.data?.content || [];

    await nextTick();
    chartKey.value++;
    barChartKey.value++;
  } catch (error) {
    console.error("Erro ao carregar dashboard:", error);
  } finally {
    loading.value = false;
  }
}

async function exportPdf() {
  exporting.value = true;
  try {
    const params = { month: selectedMonth.value, year: selectedYear.value };
    const response = await api.get('/reports/transactions/monthly', { params, responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `transacoes_${selectedMonth.value}_${selectedYear.value}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Erro ao exportar PDF:", error);
  } finally {
    exporting.value = false;
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

const categoryColors = ['#6366f1', '#10b981', '#f59e0b'];
</script>

<template>
  <div class="p-4 md:p-6 max-w-7xl mx-auto min-h-screen">

    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6 md:mb-8">
      <div class="w-full md:w-auto">
        <h2 class="text-2xl md:text-3xl font-bold text-slate-800 dark:text-slate-100 italic uppercase tracking-tighter leading-tight">
          Dashboard
        </h2>
        <p class="text-slate-400 dark:text-slate-500 text-xs font-medium mt-1">
          {{ months[selectedMonth - 1]?.label }} {{ selectedYear }}
        </p>
      </div>

      <div class="flex items-center gap-2 flex-wrap">
        <div class="flex items-center gap-1 bg-white dark:bg-slate-800 px-3 py-1.5 rounded-lg shadow-sm border border-slate-100 dark:border-slate-700 h-9">
          <select v-model="selectedMonth" class="bg-transparent text-sm font-medium outline-none w-20 text-center appearance-none cursor-pointer dark:text-slate-300">
            <option v-for="m in months" :key="m.value" :value="m.value">{{ m.label }}</option>
          </select>
          <div class="w-px h-4 bg-slate-200 dark:bg-slate-600"></div>
          <select v-model="selectedYear" class="bg-transparent text-sm font-medium outline-none text-center appearance-none w-14 cursor-pointer dark:text-slate-300">
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>

        <button @click="exportPdf" :disabled="exporting"
          class="bg-emerald-600 hover:bg-emerald-700 dark:bg-emerald-500 dark:hover:bg-emerald-600 text-white px-3 py-1.5 rounded-lg shadow-sm text-xs font-semibold flex items-center gap-1.5 h-9 disabled:opacity-50 disabled:cursor-not-allowed transition-all active:scale-95">
          <font-awesome-icon v-if="!exporting" icon="fa-solid fa-download" />
          <font-awesome-icon v-else icon="fa-solid fa-spinner" spin />
          <span class="hidden sm:inline">PDF</span>
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-4 mb-6 md:mb-8">
      <div class="bg-white dark:bg-slate-800 p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 flex flex-col items-center">
        <span class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase tracking-wide mb-1">Entradas</span>
        <h3 class="text-2xl md:text-3xl font-bold text-emerald-500">R$ {{ (summary.totalIncome || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</h3>
        <span v-if="calcChange(summary.totalIncome, summary.previousIncome) !== null"
          :class="calcChange(summary.totalIncome, summary.previousIncome) >= 0 ? 'text-emerald-500' : 'text-rose-500'"
          class="text-xs font-semibold mt-1 flex items-center gap-1">
          <font-awesome-icon :icon="calcChange(summary.totalIncome, summary.previousIncome) >= 0 ? 'fa-solid fa-arrow-up' : 'fa-solid fa-arrow-down'" class="text-[10px]" />
          {{ formatChange(calcChange(summary.totalIncome, summary.previousIncome)) }} vs mês anterior
        </span>
      </div>

      <div class="bg-white dark:bg-slate-800 p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 flex flex-col items-center">
        <span class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase tracking-wide mb-1">Saídas</span>
        <h3 class="text-2xl md:text-3xl font-bold text-rose-500">R$ {{ (summary.totalExpense || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</h3>
        <span v-if="calcChange(summary.totalExpense, summary.previousExpense) !== null"
          :class="calcChange(summary.totalExpense, summary.previousExpense) <= 0 ? 'text-emerald-500' : 'text-rose-500'"
          class="text-xs font-semibold mt-1 flex items-center gap-1">
          <font-awesome-icon :icon="calcChange(summary.totalExpense, summary.previousExpense) <= 0 ? 'fa-solid fa-arrow-down' : 'fa-solid fa-arrow-up'" class="text-[10px]" />
          {{ formatChange(Math.abs(calcChange(summary.totalExpense, summary.previousExpense))) }} vs mês anterior
        </span>
      </div>

      <div class="bg-white dark:bg-slate-800 p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 flex flex-col items-center">
        <span class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase tracking-wide mb-1">Saldo</span>
        <h3 :class="summary.balance >= 0 ? 'text-blue-600 dark:text-blue-400' : 'text-rose-600 dark:text-rose-400'" class="text-2xl md:text-3xl font-bold">
          R$ {{ (summary.balance || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}
        </h3>
        <span v-if="calcChange(summary.balance, summary.previousBalance) !== null"
          :class="calcChange(summary.balance, summary.previousBalance) >= 0 ? 'text-emerald-500' : 'text-rose-500'"
          class="text-xs font-semibold mt-1 flex items-center gap-1">
          <font-awesome-icon :icon="calcChange(summary.balance, summary.previousBalance) >= 0 ? 'fa-solid fa-arrow-up' : 'fa-solid fa-arrow-down'" class="text-[10px]" />
          {{ formatChange(calcChange(summary.balance, summary.previousBalance)) }} vs mês anterior
        </span>
      </div>
    </div>

    <div v-if="topCategories.length > 0" class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-4 mb-6 md:mb-8">
      <div v-for="(cat, index) in topCategories" :key="cat.category"
        class="bg-white dark:bg-slate-800 p-4 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 flex items-center gap-3">
        <div class="w-3 h-3 rounded-full flex-shrink-0" :style="{ backgroundColor: categoryColors[index] }"></div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-slate-700 dark:text-slate-200 truncate">{{ cat.category }}</p>
          <p class="text-xs text-slate-400 dark:text-slate-500">
            R$ {{ Number(cat.total).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}
          </p>
        </div>
        <span class="text-xs font-bold px-2 py-1 rounded-lg flex-shrink-0"
          :class="index === 0 ? 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/30 dark:text-indigo-400' : index === 1 ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400'">
          #{{ index + 1 }}
        </span>
      </div>
    </div>

    <div class="bg-white dark:bg-slate-800 p-5 md:p-8 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 flex flex-col items-center mb-6 md:mb-8">
      <h3 class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase tracking-wide mb-6 text-center italic">
        Distribuição de Gastos
      </h3>

      <div class="w-full" :class="isMobile ? 'max-w-full' : 'max-w-xl'">
        <DoughnutChart
          v-if="summary.expenseByCategory && summary.expenseByCategory.length > 0"
          :key="chartKey"
          :chartData="chartData"
          :chartOptions="chartOptions"
        />
        <div v-else-if="!loading" class="text-center py-12 text-slate-300 dark:text-slate-600 italic text-sm">
          Nenhum registro encontrado para este período.
        </div>
        <div v-else class="text-center py-12">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        </div>
      </div>
    </div>

    <div class="bg-white dark:bg-slate-800 p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 mb-6 md:mb-8">
      <h3 class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase tracking-wide mb-4 italic">
        Tendência Mensal
      </h3>
      <BarChart
        v-if="trendData.length > 0"
        :key="barChartKey"
        :chartData="trendChartData"
        :chartOptions="trendChartOptions"
      />
      <div v-else-if="!loading" class="text-center py-8 text-slate-300 dark:text-slate-600 italic text-sm">
        Sem dados de tendência para exibir.
      </div>
    </div>

    <div class="bg-white dark:bg-slate-800 p-5 md:p-6 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-xs font-medium text-slate-400 dark:text-slate-500 uppercase tracking-wide italic">
          Últimas Transações
        </h3>
        <router-link to="/transactions" class="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-1 transition-colors">
          Ver todas <font-awesome-icon icon="fa-solid fa-arrow-right-long" class="text-[10px]" />
        </router-link>
      </div>

      <div v-if="recentTransactions.length === 0 && !loading" class="text-center py-6 text-slate-300 dark:text-slate-600 italic text-sm">
        Nenhuma transação encontrada.
      </div>

      <div v-else class="space-y-2">
        <div v-for="t in recentTransactions" :key="t.id"
          class="flex items-center justify-between py-2 px-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0"
              :class="t.type === 'INCOME' ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-rose-100 text-rose-600 dark:bg-rose-900/30 dark:text-rose-400'">
              {{ t.type === 'INCOME' ? '+' : '-' }}
            </div>
            <div class="min-w-0">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-200 truncate">{{ t.description }}</p>
              <p class="text-[11px] text-slate-400 dark:text-slate-500">
                {{ new Date(t.date + 'T00:00:00').toLocaleDateString('pt-BR') }} · {{ t.categoryName }}
              </p>
            </div>
          </div>
          <span class="text-sm font-bold flex-shrink-0 ml-2"
            :class="t.type === 'INCOME' ? 'text-emerald-500' : 'text-rose-500'">
            {{ t.type === 'INCOME' ? '+' : '-' }} R$ {{ Number(t.amount).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}
          </span>
        </div>
      </div>
    </div>

  </div>
</template>