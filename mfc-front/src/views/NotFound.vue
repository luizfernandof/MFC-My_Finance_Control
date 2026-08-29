<script setup>
const homeRoute = localStorage.getItem('accessToken') ? '/dashboard' : '/';
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-4 p-6 text-center">
    <p class="text-sm font-semibold uppercase tracking-widest text-blue-500">Erro 404</p>
    <h1 class="text-3xl font-black text-slate-800 dark:text-slate-100">Página não encontrada</h1>
	    <router-link
	      :to="homeRoute"
      class="rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white hover:bg-blue-700"
    >
      Voltar ao início
    </router-link>
  </div>
</template>
