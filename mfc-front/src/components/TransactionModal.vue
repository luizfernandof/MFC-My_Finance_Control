<script setup>
import { ref, watch } from 'vue';
import BaseInput from './BaseInput.vue';

const props = defineProps({
  show: Boolean,
  editing: Boolean,
  categories: Array,
  initialData: Object,
  apiError: String
});

const emit = defineEmits(['close', 'save']);
const form = ref({ ...props.initialData });

watch(() => props.show, (isVisible) => {
  if (isVisible) {
    form.value = { ...props.initialData };
    if (form.value.categoryId) updateTypeFromCategory();
  }
});

function updateTypeFromCategory() {
  const category = props.categories.find(c => c.id === Number(form.value.categoryId));
  if (category) {
    form.value.type = category.type;
  }
}

watch(() => form.value.categoryId, updateTypeFromCategory);
watch(() => form.value.recurring, (val) => { if (val) form.value.installments = 1; });
watch(() => form.value.installments, (val) => { if (val > 1) form.value.recurring = false; });

const handleSubmit = () => {
  const sanitizedAmount = String(form.value.amount).replace(',', '.');
  const payload = {
    ...form.value,
    amount: parseFloat(sanitizedAmount) || 0,
    categoryId: Number(form.value.categoryId),
    installments: Number(form.value.installments) || 1,
    recurring: !!form.value.recurring,
    occurrences: form.value.recurring ? (Number(form.value.occurrences) || 12) : null
  };
  emit('save', payload);
};
</script>

<template>
  <div v-if="show" class="fixed inset-0 z-[100] flex items-end md:items-center justify-center p-0 md:p-4 bg-slate-900/60 backdrop-blur-sm">
    <div class="bg-white rounded-t-[2rem] md:rounded-[2.5rem] shadow-2xl w-full max-w-lg p-6 md:p-10 border border-white max-h-[90vh] md:max-h-screen overflow-y-auto">
      
      <div class="flex justify-between items-center mb-6 md:mb-8">
        <h2 class="text-xl md:text-2xl font-black text-slate-800 italic tracking-tight">
          {{ editing ? 'Editar Registro' : 'Novo Lançamento' }}
        </h2>
        <button @click="$emit('close')" class="text-slate-300 hover:text-slate-500 p-2">
          <font-awesome-icon icon="fa-solid fa-xmark" class="text-2xl" />
        </button>
      </div>

      <div v-if="apiError" class="mb-6 p-4 bg-rose-50 text-rose-600 text-[10px] font-black rounded-2xl border border-rose-100 uppercase text-center tracking-widest">
        <i class="fa-solid fa-triangle-exclamation mr-2"></i>
        {{ apiError }}
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-4 md:space-y-6">
        <BaseInput label="O que é?" v-model="form.description" placeholder="Ex: Mensalidade Academia" />

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <BaseInput label="Quanto? (R$)" v-model="form.amount" placeholder="0,00" inputmode="decimal" />
          <BaseInput label="Quando?" type="date" v-model="form.date" />
        </div>

        <div>
          <label class="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Categoria</label>
          <div class="relative">
            <select v-model="form.categoryId" @change="updateTypeFromCategory"
              class="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-bold text-slate-700 appearance-none">
              <option value="" disabled>Selecione uma categoria...</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">
                {{ c.name }} ({{ c.type === 'INCOME' ? 'Receita' : 'Despesa' }})
              </option>
            </select>
            <font-awesome-icon icon="fa-solid fa-chevron-down" class="absolute right-5 top-1/2 -translate-y-1/2 text-slate-300 pointer-events-none" />
          </div>
        </div>

        <div v-if="!editing" class="p-5 md:p-6 bg-slate-50 rounded-[1.5rem] md:rounded-[2rem] border border-slate-100 space-y-4">
          <div v-if="form.type === 'EXPENSE'" class="flex items-center justify-between">
             <span class="text-[10px] font-black text-slate-400 uppercase">Parcelar em:</span>
             <input type="number" v-model="form.installments" :disabled="form.recurring"
               class="w-16 md:w-20 p-2 bg-white border border-slate-200 rounded-xl text-center font-bold outline-none focus:ring-2 focus:ring-blue-500">
          </div>
          
          <div class="flex items-center justify-between pt-2 border-t border-slate-200/50">
            <label class="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" v-model="form.recurring" :disabled="form.installments > 1" 
                class="w-5 h-5 rounded-lg text-blue-600 border-slate-300 focus:ring-blue-500">
              <span class="text-[10px] font-black text-slate-500 uppercase">Mensalidade Fixa</span>
            </label>
            <div v-if="form.recurring" class="flex items-center gap-2">
               <input type="number" v-model="form.occurrences" 
                 class="w-14 md:w-16 p-2 bg-white border border-slate-200 rounded-xl text-center font-bold text-xs outline-none">
               <span class="text-[10px] font-bold text-slate-400 italic">meses</span>
            </div>
          </div>
        </div>

        <div class="flex flex-col md:flex-row gap-2 md:gap-4 pt-4">
          <button type="button" @click="$emit('close')" 
            class="order-2 md:order-1 flex-1 py-4 text-slate-400 font-black uppercase text-[10px] tracking-widest flex items-center justify-center gap-2 hover:bg-slate-50 rounded-2xl transition-all">
            Cancelar
          </button>
          <button type="submit" 
            class="order-1 md:order-2 flex-1 bg-blue-600 text-white py-4 rounded-2xl font-black shadow-lg shadow-blue-200 hover:bg-blue-700 transition-all transform active:scale-95 uppercase text-[10px] tracking-widest flex items-center justify-center gap-2">
            <font-awesome-icon icon="fa-solid fa-check" />
            Confirmar Registro
          </button>
        </div>
      </form>
    </div>
  </div>
</template>