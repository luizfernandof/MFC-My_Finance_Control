<script setup>
defineProps(['transactions']);
defineEmits(['edit', 'delete']);

function formatDate(dateString) {
  const [year, month, day] = dateString.split('-');
  return `${day}/${month}/${year}`;
}
</script>

<template>
  <div class="space-y-3 mb-4">
    <div v-for="t in transactions" :key="t.id" class="bg-white p-4 rounded-2xl shadow-sm border border-slate-50 flex justify-between items-center">
      <div class="flex flex-col">
        <span class="text-[9px] font-bold text-slate-400 uppercase leading-none mb-1">{{ formatDate(t.date) }}</span>
        <span class="text-xs font-black text-slate-700 leading-tight">{{ t.description }}</span>
        <span class="text-[9px] font-black text-blue-500 uppercase mt-1 tracking-tighter">{{ t.categoryName }}</span>
      </div>
      <div class="flex flex-col items-end gap-2">
        <span :class="t.type === 'INCOME' ? 'text-emerald-500' : 'text-rose-500'" class="font-black text-xs">
          {{ t.type === 'INCOME' ? '+' : '-' }} R$ {{ t.amount.toFixed(2) }}
        </span>
        <div class="flex gap-4">
          <button @click="$emit('edit', t)" class="text-blue-500 text-sm">
            <font-awesome-icon icon="fa-solid fa-pen-to-square" />
          </button>
          <button @click="$emit('delete', t)" class="text-rose-300 text-sm">
            <font-awesome-icon icon="fa-solid fa-trash-can" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>