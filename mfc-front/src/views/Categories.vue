<script setup>
import { ref, onBeforeUnmount, onMounted, watch } from 'vue';
import api from '../services/api';
import { useBreakpoint } from '../composables/useBreakpoint';
import BaseInput from '../components/BaseInput.vue';
import ConfirmModal from '../components/ConfirmModal.vue';
import PaginationControls from '../components/PaginationControls.vue';
import { getApiErrorMessage } from '../utils/apiError';
import { parsePageResponse } from '../utils/page';
import { useEscapeClose } from '../composables/useEscapeClose';

const { isMobile } = useBreakpoint();

const categories = ref([]);
const searchTerm = ref('');
const currentPage = ref(0);
const pageSize = ref(10);
const totalElements = ref(0);
const totalPages = ref(0);
const loading = ref(false);
const apiErrorMessage = ref('');
const errors = ref({ name: '' });

const showFormModal = ref(false);
const showDeleteConfirm = ref(false);
const showNoticeModal = ref(false);

const categoryForm = ref({ id: null, name: '', type: 'EXPENSE' });
const isEditing = ref(false);
const categoryToDelete = ref(null);
let requestSequence = 0;
let searchDebounce;
useEscapeClose(showFormModal, () => { showFormModal.value = false; });

function validateForm() {
  errors.value.name = '';
  if (!categoryForm.value.name.trim()) {
    errors.value.name = 'O nome da categoria é obrigatório.';
    return false;
  }
  return true;
}

async function fetchCategories() {
  const requestId = ++requestSequence;
  loading.value = true;
  try {
    const response = await api.get('/categories', {
      params: {
        search: searchTerm.value.trim(),
        page: currentPage.value,
        size: pageSize.value,
        sort: 'name,asc'
      }
    });
    if (requestId !== requestSequence) return;

    const page = parsePageResponse(response.data);
    if (page.totalPages > 0 && currentPage.value >= page.totalPages) {
      currentPage.value = page.totalPages - 1;
      return;
    }
    categories.value = page.content;
    totalElements.value = page.totalElements;
    totalPages.value = page.totalPages;
  } catch (error) {
    if (requestId === requestSequence) {
      showError(getApiErrorMessage(error, 'Erro ao carregar categorias.'));
    }
  } finally {
    if (requestId === requestSequence) loading.value = false;
  }
}

async function saveCategory() {
  if (!validateForm()) return;
  try {
    if (isEditing.value) {
      await api.put(`/categories/${categoryForm.value.id}`, categoryForm.value);
    } else {
      await api.post('/categories', categoryForm.value);
    }
    showFormModal.value = false;
    resetForm();
    fetchCategories();
  } catch (error) {
	    const msg = getApiErrorMessage(error, 'Erro inesperado ao processar.');
    showError(msg);
  }
}

function openDeleteConfirm(cat) {
  categoryToDelete.value = cat;
  showDeleteConfirm.value = true;
}

async function confirmDelete() {
  if (categoryToDelete.value) {
    try {
      await api.delete(`/categories/${categoryToDelete.value.id}`);
      showDeleteConfirm.value = false;
      fetchCategories();
    } catch (error) {
      showError(getApiErrorMessage(error, 'Não foi possível excluir. Verifique se existem transações vinculadas.'));
    }
  }
}

function openCreateModal() { resetForm(); showFormModal.value = true; }
function prepareEdit(category) {
  resetForm();
  categoryForm.value = { ...category };
  isEditing.value = true;
  showFormModal.value = true;
}
function resetForm() {
  categoryForm.value = { id: null, name: '', type: 'EXPENSE' };
  isEditing.value = false;
  apiErrorMessage.value = '';
  errors.value.name = '';
}
function showError(msg) { apiErrorMessage.value = msg; showNoticeModal.value = true; }

watch(searchTerm, () => {
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => {
    if (currentPage.value === 0) fetchCategories();
    else currentPage.value = 0;
  }, 350);
});

watch(pageSize, () => {
  if (currentPage.value === 0) fetchCategories();
  else currentPage.value = 0;
});

watch(currentPage, fetchCategories);

onBeforeUnmount(() => clearTimeout(searchDebounce));

onMounted(fetchCategories);
</script>

<template>
  <div class="p-4 md:p-6 max-w-5xl mx-auto min-h-screen">

    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 mb-4">
      <div class="w-full md:w-auto">
        <h2 class="text-2xl md:text-3xl font-bold text-slate-800 dark:text-slate-100 italic uppercase tracking-tighter leading-tight">Categorias</h2>
        <p class="text-slate-400 dark:text-slate-500 text-xs font-medium mt-1">Gerencie seus grupos de custo</p>
      </div>

      <button @click="openCreateModal"
        class="bg-blue-600 dark:bg-blue-500 text-white px-4 py-2 rounded-lg font-semibold shadow-sm hover:shadow-md active:scale-95 transition-all text-xs uppercase flex items-center gap-1.5 h-9">
        <font-awesome-icon icon="fa-solid fa-plus" class="text-sm" />
        <span>Nova Categoria</span>
      </button>
	    </div>

    <div class="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
      <div class="relative w-full sm:max-w-md">
        <font-awesome-icon icon="fa-solid fa-magnifying-glass" class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-300 dark:text-slate-500" />
        <input
          v-model="searchTerm"
          type="search"
          maxlength="100"
          aria-label="Buscar categorias"
          placeholder="Buscar categoria por nome"
          class="h-11 w-full rounded-xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-medium text-slate-700 outline-none transition-all placeholder:text-slate-300 focus:border-blue-400 focus:ring-2 focus:ring-blue-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200 dark:placeholder:text-slate-500"
        />
      </div>
      <span class="text-xs font-medium text-slate-400 dark:text-slate-500">
        {{ totalElements }} {{ totalElements === 1 ? 'categoria' : 'categorias' }}
      </span>
    </div>

	    <div v-if="loading" class="py-12 text-center text-sm font-medium text-slate-400">Carregando categorias...</div>
	    <div v-else-if="categories.length === 0" class="rounded-2xl border border-dashed border-slate-200 p-10 text-center text-sm text-slate-400 dark:border-slate-700">
	      {{ searchTerm.trim() ? 'Nenhuma categoria encontrada para a busca.' : 'Nenhuma categoria cadastrada.' }}
	    </div>

	    <div v-else-if="!isMobile" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
      <table class="w-full text-left">
        <thead class="bg-slate-50 dark:bg-slate-700/50 text-xs font-semibold text-slate-400 dark:text-slate-400 uppercase tracking-wide">
          <tr>
            <th class="px-6 py-4">Nome</th>
            <th class="px-6 py-4">Tipo</th>
            <th class="px-6 py-4 text-right">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50 dark:divide-slate-700/50">
          <tr v-for="cat in categories" :key="cat.id" class="hover:bg-slate-50/50 dark:hover:bg-slate-700/30 transition-colors">
            <td class="px-6 py-4 font-semibold text-slate-700 dark:text-slate-200 text-sm italic">{{ cat.name }}</td>
            <td class="px-6 py-4">
              <span :class="cat.type === 'INCOME' ? 'bg-emerald-50 text-emerald-600 border-emerald-100 dark:bg-emerald-900/30 dark:text-emerald-400 dark:border-emerald-800' : 'bg-rose-50 text-rose-600 border-rose-100 dark:bg-rose-900/30 dark:text-rose-400 dark:border-rose-800'"
                class="px-3 py-1 rounded-full font-semibold uppercase border text-xs">
                {{ cat.type === 'INCOME' ? 'Receita' : 'Despesa' }}
              </span>
            </td>
            <td class="px-6 py-4 text-right">
              <div class="flex justify-end gap-4">
                <button @click="prepareEdit(cat)" class="text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 p-1"><font-awesome-icon icon="fa-solid fa-pen-to-square" /></button>
                <button @click="openDeleteConfirm(cat)" class="text-rose-300 hover:text-rose-500 dark:text-rose-400 dark:hover:text-rose-300 p-1"><font-awesome-icon icon="fa-solid fa-trash-can" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else-if="!loading && categories.length > 0" class="space-y-2">
      <div v-for="cat in categories" :key="cat.id" class="bg-white dark:bg-slate-800 p-4 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 flex justify-between items-center">
        <div class="flex flex-col">
          <span class="text-sm font-semibold text-slate-700 dark:text-slate-200 italic">{{ cat.name }}</span>
          <span :class="cat.type === 'INCOME' ? 'text-emerald-500 dark:text-emerald-400' : 'text-rose-500 dark:text-rose-400'" class="text-xs font-medium mt-0.5">
            {{ cat.type === 'INCOME' ? 'Receita' : 'Despesa' }}
          </span>
        </div>
        <div class="flex gap-3">
          <button @click="prepareEdit(cat)" class="text-blue-500 dark:text-blue-400 p-2"><font-awesome-icon icon="fa-solid fa-pen-to-square" /></button>
          <button @click="openDeleteConfirm(cat)" class="text-rose-300 dark:text-rose-400 p-2"><font-awesome-icon icon="fa-solid fa-trash-can" /></button>
        </div>
      </div>
    </div>

    <PaginationControls
      v-if="!loading && totalElements > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total-pages="totalPages"
      :total-elements="totalElements"
    />

    <div v-if="showFormModal" role="dialog" aria-modal="true" aria-labelledby="category-modal-title" class="fixed inset-0 z-[100] flex items-end md:items-center justify-center p-0 md:p-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="bg-white dark:bg-slate-800 rounded-t-3xl md:rounded-2xl shadow-2xl w-full max-w-md p-6 border border-white dark:border-slate-700">
        <div class="flex justify-between items-center mb-6">
          <h2 id="category-modal-title" class="text-xl font-bold text-slate-800 dark:text-slate-100 italic tracking-tight">{{ isEditing ? 'Editar Categoria' : 'Nova Categoria' }}</h2>
          <button type="button" aria-label="Fechar categoria" @click="showFormModal = false" class="text-slate-300 hover:text-slate-500 dark:text-slate-500 dark:hover:text-slate-300 text-xl"><font-awesome-icon icon="fa-solid fa-xmark" /></button>
        </div>

        <form @submit.prevent="saveCategory" class="space-y-5">
          <BaseInput label="Nome da Categoria" v-model="categoryForm.name" placeholder="Ex: Lazer" :error="errors.name" />
          <div>
            <label for="category-type" class="block text-xs font-semibold text-slate-500 dark:text-slate-400 mb-2 ml-1">Tipo de Fluxo</label>
            <select id="category-type" v-model="categoryForm.type" class="w-full px-4 py-3 bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none font-medium text-slate-700 dark:text-slate-200 appearance-none">
              <option value="EXPENSE">Despesa (Saída)</option>
              <option value="INCOME">Receita (Entrada)</option>
            </select>
          </div>
          <div class="flex gap-2 pt-2">
            <button type="button" @click="showFormModal = false" class="flex-1 py-3 text-slate-400 dark:text-slate-500 font-medium text-sm rounded-xl border border-slate-200 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700">Cancelar</button>
            <button type="submit" class="flex-1 bg-blue-600 dark:bg-blue-500 text-white py-3 rounded-xl font-semibold shadow-sm hover:bg-blue-700 dark:hover:bg-blue-600 transition-all text-sm flex items-center justify-center gap-2">
              <font-awesome-icon icon="fa-solid fa-check" /> Salvar
            </button>
          </div>
        </form>
      </div>
    </div>

    <ConfirmModal :show="showDeleteConfirm" title="Excluir Categoria?" :message="`Deseja realmente apagar a categoria '${categoryToDelete?.name}'?`" confirmText="Sim, Excluir" @close="showDeleteConfirm = false" @confirm="confirmDelete" />
    <ConfirmModal :show="showNoticeModal" title="Atenção" :message="apiErrorMessage" confirmText="Entendi" variant="primary" @close="showNoticeModal = false" @confirm="showNoticeModal = false" />
  </div>
</template>
