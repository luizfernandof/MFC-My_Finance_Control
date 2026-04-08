<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '../services/api';
import { useBreakpoint } from '../composables/useBreakpoint';

// Componentes
import TransactionTableDesktop from '../components/TransactionTableDesktop.vue';
import TransactionCardsMobile from '../components/TransactionCardsMobile.vue';
import TransactionModal from '../components/TransactionModal.vue';
import ConfirmModal from '../components/ConfirmModal.vue';

const { isMobile } = useBreakpoint();

// --- ESTADOS ---
const transactions = ref([]);
const categories = ref([]);
const selectedMonth = ref(new Date().getMonth() + 1);
const selectedYear = ref(new Date().getFullYear());

// ORDENAÇÃO
const selectedSort = ref('date,desc');
const sortOptions = [
  { value: 'date,desc', label: 'Data (recente)' },
  { value: 'date,asc', label: 'Data (antiga)' },
  { value: 'amount,asc', label: 'Valor (menor)' },
  { value: 'amount,desc', label: 'Valor (maior)' }
];

// PAGINAÇÃO
const currentPage = ref(0);
const pageSize = ref(10);
const totalElements = ref(0);
const totalPages = ref(0);

const loading = ref(false);
const showFormModal = ref(false);
const isEditing = ref(false);
const apiErrorMessage = ref('');
const showConfirmModal = ref(false);
const transactionToDelete = ref(null);

const initialForm = {
  id: null, description: '', amount: '', date: new Date().toISOString().split('T')[0],
  type: '', categoryId: '', installments: 1, recurring: false, occurrences: 12
};
const transactionForm = ref({ ...initialForm });

// --- LÓGICA DE DADOS ---
async function fetchCategories() {
  try {
    const responseCategories = await api.get('/categories');
    categories.value = responseCategories.data || [];
  } catch (e) {
    console.error("Erro ao carregar categorias:", e);
  }
}

async function fetchTransactions() {
  loading.value = true;
  try {
    const params = {
      month: selectedMonth.value,
      year: selectedYear.value,
      page: currentPage.value,
      size: pageSize.value,
      sort: selectedSort.value
    };

    const responseTransactions = await api.get('/transactions', { params });

    // TRANSACTIONS
    const data = responseTransactions.data || {};
    transactions.value = data.content || [];

    // PAGINATION
    const page = data.page || {};
    totalElements.value = Number(page.totalElements) || 0;
    totalPages.value = Number(page.totalPages) || 0;
  } catch (e) {
    console.error("Erro ao carregar transações:", e);
  } finally {
    loading.value = false;
  }
}

// WATCHERS
watch([selectedMonth, selectedYear, pageSize, currentPage, selectedSort], () => {
  fetchTransactions();
});

watch([selectedMonth, selectedYear, pageSize, selectedSort], () => {
  currentPage.value = 0;
});

watch(isMobile, (newVal) => {
  pageSize.value = newVal ? 1000 : 10;
}, { immediate: true });

// ACTION(EDIT, DELETE, SAVE)
function openCreate() {
  isEditing.value = false;
  transactionForm.value = { ...initialForm };
  showFormModal.value = true;
}

function prepareEdit(t) {
  isEditing.value = true;
  const categoryId = categories.value.find(c => c.name === t.categoryName)?.id || '';
  transactionForm.value = { ...t, categoryId };
  showFormModal.value = true;
}

async function handleSave(payload) {
  apiErrorMessage.value = '';
  try {
    if (isEditing.value) {
      await api.put(`/transactions/${payload.id}`, payload);
    } else {
      await api.post('/transactions', payload);
    }
    showFormModal.value = false;
    fetchTransactions();
  } catch (error) {
    apiErrorMessage.value = error.response?.data?.message || "Erro no servidor.";
  }
}

function openDeleteConfirm(t) {
  transactionToDelete.value = t;
  showConfirmModal.value = true;
}

async function confirmDelete() {
  if (!transactionToDelete.value) return;
  try {
    // Se for transação recorrente (tem groupId), deleta apenas a partir dela
    const endpoint = transactionToDelete.value.groupId
      ? `/transactions/${transactionToDelete.value.id}/recurrent-forward`
      : `/transactions/${transactionToDelete.value.id}`;
    await api.delete(endpoint);
    showConfirmModal.value = false;
    transactionToDelete.value = null;
    fetchTransactions();
  } catch (e) {
    console.error(e);
  }
}

onMounted(async () => {
  await fetchCategories();
  await fetchTransactions();
});

const months = [
  { value: 1, label: 'Janeiro' }, { value: 2, label: 'Fevereiro' }, { value: 3, label: 'Março' },
  { value: 4, label: 'Abril' }, { value: 5, label: 'Maio' }, { value: 6, label: 'Junho' },
  { value: 7, label: 'Julho' }, { value: 8, label: 'Agosto' }, { value: 9, label: 'Setembro' },
  { value: 10, label: 'Outubro' }, { value: 11, label: 'Novembro' }, { value: 12, label: 'Dezembro' }
];
const years = Array.from({ length: 6 }, (_, i) => new Date().getFullYear() - i);
</script>

<template>
  <div class="p-3 md:p-6 max-w-7xl mx-auto min-h-screen flex flex-col">

    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6 md:mb-10">
      <div class="w-full md:w-auto text-left">
        <h2 class="text-2xl md:text-3xl font-black text-slate-800 italic uppercase tracking-tighter leading-tight">
          Transações
        </h2>
        <p class="text-slate-400 text-[9px] font-black uppercase tracking-[0.2em]">
          {{ months[selectedMonth - 1].label }} {{ selectedYear }}
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2 w-full md:w-auto">

        <!-- PAGINAÇÃO (setas) -->
        <div v-if="!isMobile"
          class="flex items-center bg-white px-3 py-1 rounded-lg border border-slate-100 shadow-sm h-9">
          <button @click="currentPage--" :disabled="currentPage === 0 || totalPages === 0"
            class="px-2 text-blue-600 disabled:text-slate-200 disabled:opacity-50 disabled:cursor-not-allowed hover:scale-110 transition-all active:scale-95">
            <font-awesome-icon icon="arrow-left" class="text-sm" />
          </button>
          <span class="text-xs font-medium text-slate-500 mx-2 whitespace-nowrap">
            {{ totalPages ? (currentPage + 1) : 0 }}/{{ totalPages || 0 }}
          </span>
          <button @click="currentPage++" :disabled="currentPage >= totalPages - 1 || totalPages === 0"
            class="px-2 text-blue-600 disabled:text-slate-200 disabled:opacity-50 disabled:cursor-not-allowed hover:scale-110 transition-all active:scale-95">
            <font-awesome-icon icon="arrow-right" class="text-sm" />
          </button>
        </div>

        <!-- EXIBIR -->
        <div v-if="!isMobile"
          class="flex items-center bg-white px-3 py-1 rounded-lg border border-slate-100 shadow-sm h-9">
          <span class="text-[10px] font-semibold text-slate-400 uppercase tracking-wide mr-2">Exibir</span>
          <select v-model="pageSize"
            class="bg-transparent text-sm font-medium outline-none cursor-pointer text-blue-600 appearance-none w-10 text-center">
            <option :value="5">05</option>
            <option :value="10">10</option>
            <option :value="15">15</option>
            <option :value="30">30</option>
            <option :value="50">50</option>
          </select>
        </div>

        <!-- ORDENAR -->
        <div v-if="!isMobile"
          class="flex items-center bg-white px-3 py-1 rounded-lg border border-slate-100 shadow-sm h-9">
          <span class="text-[10px] font-semibold text-slate-400 uppercase tracking-wide mr-2">Ordenar</span>
          <select v-model="selectedSort"
            class="bg-transparent text-sm font-medium outline-none cursor-pointer text-blue-600 appearance-none w-28">
            <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>

        <!-- MÊS/ANO -->
        <div class="flex items-center gap-1 bg-white px-3 py-1 rounded-lg shadow-sm border border-slate-100 h-9">
          <select v-model="selectedMonth"
            class="bg-transparent text-sm font-medium outline-none w-20 text-center appearance-none cursor-pointer">
            <option v-for="m in months" :key="m.value" :value="m.value">{{ m.label }}</option>
          </select>
          <div class="w-px h-4 bg-slate-200"></div>
          <select v-model="selectedYear"
            class="bg-transparent text-sm font-medium outline-none text-center appearance-none w-14 cursor-pointer">
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>

        <!-- NOVO -->
        <button @click="openCreate"
          class="bg-blue-600 text-white px-4 py-1 rounded-lg font-semibold shadow-sm active:scale-95 transition-all text-xs uppercase flex items-center gap-1.5 h-9">
          <font-awesome-icon icon="plus" class="text-sm" />
          <span>Novo</span>
        </button>

        <!-- Total registros (mobile) -->
        <span v-if="isMobile" class="text-xs font-medium text-slate-400 self-center ml-2">
          {{ totalElements }} registros
        </span>
      </div>
    </div>

    <div class="flex-grow">
      <TransactionTableDesktop v-if="!isMobile" :transactions="transactions" @edit="prepareEdit"
        @delete="openDeleteConfirm" />
      <TransactionCardsMobile v-else :transactions="transactions" @edit="prepareEdit" @delete="openDeleteConfirm" />
    </div>

    <TransactionModal :show="showFormModal" :editing="isEditing" :categories="categories" :initialData="transactionForm"
      :apiError="apiErrorMessage" @close="showFormModal = false" @save="handleSave" />

    <ConfirmModal :show="showConfirmModal" title="Excluir Registro?"
      :message="`Deseja realmente excluir '${transactionToDelete?.description}'?`" confirmText="Sim, Excluir"
      @close="showConfirmModal = false" @confirm="confirmDelete" />

  </div>
</template>
