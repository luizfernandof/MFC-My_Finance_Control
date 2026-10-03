<script setup>
import { ref, onBeforeUnmount, onMounted, watch } from 'vue';
import api from '../services/api';
import { useBreakpoint } from '../composables/useBreakpoint';

import TransactionTableDesktop from '../components/TransactionTableDesktop.vue';
import TransactionCardsMobile from '../components/TransactionCardsMobile.vue';
import TransactionModal from '../components/TransactionModal.vue';
import ConfirmModal from '../components/ConfirmModal.vue';
import GroupDeleteModal from '../components/GroupDeleteModal.vue';
import PaginationControls from '../components/PaginationControls.vue';
import { months, years } from '../composables/usePeriodOptions';
import { getApiErrorMessage } from '../utils/apiError';
import { parsePageResponse } from '../utils/page';

const { isMobile } = useBreakpoint();

const transactions = ref([]);
const categories = ref([]);
const selectedMonth = ref(new Date().getMonth() + 1);
const selectedYear = ref(new Date().getFullYear());
const searchTerm = ref('');

const selectedSort = ref('date,desc');
const sortOptions = [
  { value: 'date,desc', label: 'Data (recente)' },
  { value: 'date,asc', label: 'Data (antiga)' },
  { value: 'amount,asc', label: 'Valor (menor)' },
  { value: 'amount,desc', label: 'Valor (maior)' }
];

const currentPage = ref(0);
const pageSize = ref(10);
const totalElements = ref(0);
const totalPages = ref(0);

const loading = ref(false);
const showFormModal = ref(false);
const isEditing = ref(false);
const apiErrorMessage = ref('');
const showConfirmModal = ref(false);
const showGroupDeleteModal = ref(false);
const transactionToDelete = ref(null);
const loadError = ref('');
let requestSequence = 0;
let searchDebounce;

const initialForm = {
  id: null, description: '', amount: '', date: new Date().toISOString().split('T')[0],
  type: '', categoryId: '', installments: 1, recurring: false, occurrences: 12
};
const transactionForm = ref({ ...initialForm });

async function fetchCategories() {
  try {
    const responseCategories = await api.get('/categories/options');
    categories.value = responseCategories.data || [];
	  } catch (e) {
	    loadError.value = getApiErrorMessage(e, 'Não foi possível carregar as categorias.');
  }
}

async function fetchTransactions() {
	  const requestId = ++requestSequence;
	  loading.value = true;
	  loadError.value = '';
  try {
    const params = {
      month: selectedMonth.value,
      year: selectedYear.value,
      search: searchTerm.value.trim(),
      page: currentPage.value,
      size: pageSize.value,
      sort: selectedSort.value
    };

    const responseTransactions = await api.get('/transactions', { params });

	    if (requestId !== requestSequence) return;
	    const page = parsePageResponse(responseTransactions.data);
    if (page.totalPages > 0 && currentPage.value >= page.totalPages) {
      currentPage.value = page.totalPages - 1;
      return;
    }
    transactions.value = page.content;
    totalElements.value = page.totalElements;
    totalPages.value = page.totalPages;
	  } catch (e) {
	    if (requestId === requestSequence) {
	      loadError.value = getApiErrorMessage(e, 'Não foi possível carregar as transações.');
	    }
	  } finally {
	    if (requestId === requestSequence) loading.value = false;
	  }
}

watch([selectedMonth, selectedYear, pageSize, selectedSort], () => {
	  if (currentPage.value === 0) fetchTransactions();
	  else currentPage.value = 0;
});

watch(currentPage, fetchTransactions);

watch(searchTerm, () => {
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => {
    if (currentPage.value === 0) fetchTransactions();
    else currentPage.value = 0;
  }, 350);
});

onBeforeUnmount(() => clearTimeout(searchDebounce));

function openCreate() {
	  apiErrorMessage.value = '';
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
	    apiErrorMessage.value = getApiErrorMessage(error, 'Erro no servidor.');
	  }
}

function openDeleteConfirm(t) {
	  transactionToDelete.value = t;
	  if (t.groupId) showGroupDeleteModal.value = true;
	  else showConfirmModal.value = true;
}

async function confirmDelete(scope = 'single') {
	  if (!transactionToDelete.value) return;
	  try {
	    const base = `/transactions/${transactionToDelete.value.id}`;
	    const endpoint = scope === 'all' ? `${base}/group` : scope === 'forward' ? `${base}/group-forward` : base;
	    await api.delete(endpoint);
	    showConfirmModal.value = false;
	    showGroupDeleteModal.value = false;
	    transactionToDelete.value = null;
	    fetchTransactions();
	  } catch (e) {
	    loadError.value = getApiErrorMessage(e, 'Não foi possível excluir a transação.');
	  }
}

onMounted(async () => {
  await fetchCategories();
  await fetchTransactions();
});

</script>

<template>
  <div class="p-3 md:p-6 max-w-7xl mx-auto min-h-screen">

    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 mb-4">
      <div class="w-full md:w-auto text-left">
        <h2 class="text-2xl md:text-3xl font-black text-slate-800 dark:text-slate-100 italic uppercase tracking-tighter leading-tight">
          Transações
        </h2>
        <p class="text-slate-400 dark:text-slate-500 text-[9px] font-black uppercase tracking-[0.2em]">
          {{ months[selectedMonth - 1].label }} {{ selectedYear }}
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2 w-full md:w-auto">
        <div v-if="!isMobile"
          class="flex items-center bg-white dark:bg-slate-800 px-3 py-1 rounded-lg border border-slate-100 dark:border-slate-700 shadow-sm h-9">
          <span class="text-[10px] font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wide mr-2">Ordenar</span>
          <select v-model="selectedSort"
            class="bg-transparent text-sm font-medium outline-none cursor-pointer text-blue-600 dark:text-blue-400 appearance-none w-28">
            <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>

        <div class="flex items-center gap-1 bg-white dark:bg-slate-800 px-3 py-1 rounded-lg shadow-sm border border-slate-100 dark:border-slate-700 h-9">
          <select v-model="selectedMonth"
            class="bg-transparent text-sm font-medium outline-none w-20 text-center appearance-none cursor-pointer dark:text-slate-300">
            <option v-for="m in months" :key="m.value" :value="m.value">{{ m.label }}</option>
          </select>
          <div class="w-px h-4 bg-slate-200 dark:bg-slate-600"></div>
          <select v-model="selectedYear"
            class="bg-transparent text-sm font-medium outline-none text-center appearance-none w-14 cursor-pointer dark:text-slate-300">
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>

        <button @click="openCreate"
          class="bg-blue-600 dark:bg-blue-500 text-white px-4 py-1 rounded-lg font-semibold shadow-sm active:scale-95 transition-all text-xs uppercase flex items-center gap-1.5 h-9">
          <font-awesome-icon icon="plus" class="text-sm" />
          <span>Novo</span>
        </button>

      </div>
	    </div>

    <div class="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div class="relative w-full sm:max-w-lg">
        <font-awesome-icon icon="fa-solid fa-magnifying-glass" class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-300 dark:text-slate-500" />
        <input
          v-model="searchTerm"
          type="search"
          maxlength="100"
          aria-label="Buscar transações"
          placeholder="Buscar por descrição ou categoria"
          class="h-11 w-full rounded-xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-medium text-slate-700 outline-none transition-all placeholder:text-slate-300 focus:border-blue-400 focus:ring-2 focus:ring-blue-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200 dark:placeholder:text-slate-500"
        />
      </div>
      <span class="text-xs font-medium text-slate-400 dark:text-slate-500">
        {{ totalElements }} {{ totalElements === 1 ? 'transação' : 'transações' }}
      </span>
    </div>

	    <div v-if="loadError" role="alert" class="mb-4 rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm font-semibold text-rose-600 dark:border-rose-800 dark:bg-rose-900/20 dark:text-rose-400">
	      {{ loadError }}
	    </div>

	    <div v-if="loading" class="py-16 text-center text-sm font-medium text-slate-400">
	      Carregando transações...
	    </div>
	    <div v-else-if="transactions.length === 0" class="rounded-2xl border border-dashed border-slate-200 py-16 text-center text-sm text-slate-400 dark:border-slate-700">
	      {{ searchTerm.trim() ? 'Nenhuma transação encontrada para a busca neste período.' : 'Nenhuma transação encontrada para este período.' }}
	    </div>
	    <div v-else>
      <TransactionTableDesktop v-if="!isMobile" :transactions="transactions" @edit="prepareEdit"
        @delete="openDeleteConfirm" />
      <TransactionCardsMobile v-else :transactions="transactions" @edit="prepareEdit" @delete="openDeleteConfirm" />
    </div>

    <PaginationControls
      v-if="!loading && totalElements > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total-pages="totalPages"
      :total-elements="totalElements"
    />

    <TransactionModal :show="showFormModal" :editing="isEditing" :categories="categories" :initialData="transactionForm"
      :apiError="apiErrorMessage" @close="showFormModal = false" @save="handleSave" />

	    <ConfirmModal :show="showConfirmModal" title="Excluir Registro?"
      :message="`Deseja realmente excluir '${transactionToDelete?.description}'?`" confirmText="Sim, Excluir"
	      @close="showConfirmModal = false" @confirm="confirmDelete" />

	    <GroupDeleteModal :show="showGroupDeleteModal" :transaction="transactionToDelete"
	      @close="showGroupDeleteModal = false" @confirm="confirmDelete" />

  </div>
</template>
