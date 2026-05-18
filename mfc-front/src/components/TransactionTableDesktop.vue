<script setup>
defineProps(['transactions']);
defineEmits(['edit', 'delete']);

function formatDate(dateString) {
  const [year, month, day] = dateString.split('-');
  return `${day}/${month}/${year}`;
}
</script>

<template>
  <div class="bg-white dark:bg-slate-800 rounded-[2.5rem] shadow-xl overflow-hidden border border-slate-50 dark:border-slate-700 mb-4">
    <table class="w-full text-left border-collapse">
      <thead class="bg-slate-50/50 dark:bg-slate-700/50 text-[9px] font-black uppercase text-slate-400 dark:text-slate-400 tracking-[0.2em]">
        <tr>
          <th class="px-8 py-5 hidden lg:table-cell">Data</th>
          <th class="px-8 py-5">Descrição</th>
          <th class="px-8 py-5 hidden md:table-cell">Categoria</th>
          <th class="px-8 py-5 text-right">Valor</th>
          <th class="px-8 py-5 text-center">Ações</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-50 dark:divide-slate-700/50">
        <tr v-for="t in transactions" :key="t.id" class="hover:bg-slate-50/50 dark:hover:bg-slate-700/30 transition-colors">
          <td class="px-8 py-5 text-xs font-bold text-slate-400 dark:text-slate-500 italic hidden lg:table-cell">{{ formatDate(t.date) }}</td>
          <td class="px-8 py-5 text-sm font-black text-slate-700 dark:text-slate-200 leading-tight">
            {{ t.description }}
            <font-awesome-icon v-if="t.groupId" icon="fa-solid fa-repeat" class="ml-2 text-blue-400 dark:text-blue-500 text-[10px]" />
          </td>
          <td class="px-8 py-5 hidden md:table-cell">
            <span class="px-3 py-1 rounded-full font-black uppercase border text-[9px]"
              :class="t.type === 'INCOME' ? 'bg-emerald-50 text-emerald-600 border-emerald-100 dark:bg-emerald-900/30 dark:text-emerald-400 dark:border-emerald-800' : 'bg-rose-50 text-rose-600 border-rose-100 dark:bg-rose-900/30 dark:text-rose-400 dark:border-rose-800'">
              {{ t.categoryName }}
            </span>
          </td>
          <td class="px-8 py-5 text-sm font-black text-right tracking-tight" :class="t.type === 'INCOME' ? 'text-emerald-500 dark:text-emerald-400' : 'text-rose-500 dark:text-rose-400'">
            {{ t.type === 'INCOME' ? '+' : '-' }} R$ {{ t.amount.toFixed(2) }}
          </td>
          <td class="px-8 py-5 text-center">
            <div class="flex justify-center gap-4">
              <button @click="$emit('edit', t)" class="text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 transition-colors">
                <font-awesome-icon icon="fa-solid fa-pen-to-square" />
              </button>
              <button @click="$emit('delete', t)" class="text-rose-300 hover:text-rose-500 dark:text-rose-400 dark:hover:text-rose-300 transition-colors">
                <font-awesome-icon icon="fa-solid fa-trash-can" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>