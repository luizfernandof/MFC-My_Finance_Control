<script setup>
import { ref, onMounted } from 'vue';
import api from '../services/api';
import { useBreakpoint } from '../composables/useBreakpoint';
import BaseInput from '../components/BaseInput.vue';
import ConfirmModal from '../components/ConfirmModal.vue';

const { isMobile } = useBreakpoint();

// --- ESTADOS ---
const categories = ref([]);
const loading = ref(false);
const apiErrorMessage = ref('');
const errors = ref({ name: '' });

const showFormModal = ref(false);
const showDeleteConfirm = ref(false);
const showNoticeModal = ref(false);

const categoryForm = ref({ id: null, name: '', type: 'EXPENSE' });
const isEditing = ref(false);
const categoryToDelete = ref(null);

// --- VALIDAÇÃO ---
function validateForm() {
  errors.value.name = '';
  if (!categoryForm.value.name.trim()) {
    errors.value.name = 'O nome da categoria é obrigatório.';
    return false;
  }
  return true;
}

// --- CARREGAMENTO ---
async function fetchCategories() {
  loading.value = true;
  try {
    const response = await api.get('/categories');
    categories.value = response.data;
  } catch (error) {
    showError("Erro ao carregar categorias");
  } finally {
    loading.value = false;
  }
}

// --- SALVAR ---
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
    const msg = error.response?.data?.message || "Erro inesperado ao processar.";
    showError(msg);
  }
}

// --- EXCLUSÃO ---
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
      showError("Não foi possível excluir. Verifique se existem transações vinculadas.");
    }
  }
}

// --- AUXILIARES ---
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

onMounted(fetchCategories);
</script>

<template>
  <div class="p-4 md:p-6 max-w-5xl mx-auto min-h-screen">

    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6 md:mb-8">
      <div class="w-full md:w-auto">
        <h2 class="text-2xl md:text-3xl font-bold text-slate-800 italic uppercase tracking-tighter leading-tight">Categorias</h2>
        <p class="text-slate-400 text-xs font-medium mt-1">Gerencie seus grupos de custo</p>
      </div>

      <button @click="openCreateModal"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg font-semibold shadow-sm hover:shadow-md active:scale-95 transition-all text-xs uppercase flex items-center gap-1.5 h-9">
        <font-awesome-icon icon="fa-solid fa-plus" class="text-sm" />
        <span>Nova Categoria</span>
      </button>
    </div>

    <div v-if="!isMobile" class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <table class="w-full text-left">
        <thead class="bg-slate-50 text-xs font-semibold text-slate-400 uppercase tracking-wide">
          <tr>
            <th class="px-6 py-4">Nome</th>
            <th class="px-6 py-4">Tipo</th>
            <th class="px-6 py-4 text-right">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="cat in categories" :key="cat.id" class="hover:bg-slate-50/50 transition-colors">
            <td class="px-6 py-4 font-semibold text-slate-700 text-sm italic">{{ cat.name }}</td>
            <td class="px-6 py-4">
              <span :class="cat.type === 'INCOME' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-rose-50 text-rose-600 border-rose-100'"
                class="px-3 py-1 rounded-full font-semibold uppercase border text-xs">
                {{ cat.type === 'INCOME' ? 'Receita' : 'Despesa' }}
              </span>
            </td>
            <td class="px-6 py-4 text-right">
              <div class="flex justify-end gap-4">
                <button @click="prepareEdit(cat)" class="text-blue-500 hover:text-blue-700 p-1"><font-awesome-icon icon="fa-solid fa-pen-to-square" /></button>
                <button @click="openDeleteConfirm(cat)" class="text-rose-300 hover:text-rose-500 p-1"><font-awesome-icon icon="fa-solid fa-trash-can" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="space-y-2">
      <div v-for="cat in categories" :key="cat.id" class="bg-white p-4 rounded-xl shadow-sm border border-slate-100 flex justify-between items-center">
        <div class="flex flex-col">
          <span class="text-sm font-semibold text-slate-700 italic">{{ cat.name }}</span>
          <span :class="cat.type === 'INCOME' ? 'text-emerald-500' : 'text-rose-500'" class="text-xs font-medium mt-0.5">
            {{ cat.type === 'INCOME' ? 'Receita' : 'Despesa' }}
          </span>
        </div>
        <div class="flex gap-3">
          <button @click="prepareEdit(cat)" class="text-blue-500 p-2"><font-awesome-icon icon="fa-solid fa-pen-to-square" /></button>
          <button @click="openDeleteConfirm(cat)" class="text-rose-300 p-2"><font-awesome-icon icon="fa-solid fa-trash-can" /></button>
        </div>
      </div>
    </div>

    <div v-if="showFormModal" class="fixed inset-0 z-[100] flex items-end md:items-center justify-center p-0 md:p-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="bg-white rounded-t-3xl md:rounded-2xl shadow-2xl w-full max-w-md p-6 border border-white">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-xl font-bold text-slate-800 italic tracking-tight">{{ isEditing ? 'Editar Categoria' : 'Nova Categoria' }}</h2>
          <button @click="showFormModal = false" class="text-slate-300 hover:text-slate-500 text-xl"><font-awesome-icon icon="fa-solid fa-xmark" /></button>
        </div>

        <form @submit.prevent="saveCategory" class="space-y-5">
          <BaseInput label="Nome da Categoria" v-model="categoryForm.name" placeholder="Ex: Lazer" :error="errors.name" />
          <div>
            <label class="block text-xs font-semibold text-slate-500 mb-2 ml-1">Tipo de Fluxo</label>
            <select v-model="categoryForm.type" class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none font-medium text-slate-700 appearance-none">
              <option value="EXPENSE">Despesa (Saída)</option>
              <option value="INCOME">Receita (Entrada)</option>
            </select>
          </div>
          <div class="flex gap-2 pt-2">
            <button type="button" @click="showFormModal = false" class="flex-1 py-3 text-slate-400 font-medium text-sm rounded-xl border border-slate-200">Cancelar</button>
            <button type="submit" class="flex-1 bg-blue-600 text-white py-3 rounded-xl font-semibold shadow-sm hover:bg-blue-700 transition-all text-sm flex items-center justify-center gap-2">
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
