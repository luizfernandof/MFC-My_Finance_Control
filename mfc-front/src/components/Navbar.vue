<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import ReportsModal from './ReportsModal.vue';

const router = useRouter();
const isMenuOpen = ref(false);
const showReportsModal = ref(false);

function handleLogout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  router.push('/');
}

const closeMenu = () => {
  isMenuOpen.value = false;
};
</script>

<template>
  <nav class="bg-blue-600 shadow-lg mb-6">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        
        <div class="flex items-center">
          <router-link to="/dashboard" class="text-white font-black text-2xl tracking-tighter mr-8">
            My Finance Control
          </router-link>
          
          <div class="hidden md:flex space-x-2">
            <router-link to="/dashboard"
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all"
              active-class="bg-blue-800 text-white shadow-inner">
              Dashboard
            </router-link>
            <router-link to="/transactions"
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all"
              active-class="bg-blue-800 text-white shadow-inner">
              Transações
            </router-link>
            <router-link to="/categories"
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all"
              active-class="bg-blue-800 text-white shadow-inner">
              Categorias
            </router-link>
            <button @click="showReportsModal = true"
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all">
              Relatórios
            </button>
          </div>
        </div>

        <div class="hidden md:flex items-center gap-3">
          <router-link to="/profile" 
            class="text-white border border-blue-400 hover:bg-blue-500 px-4 py-2 rounded-xl text-sm font-bold transition-all">
            Meu Perfil
          </router-link>
          <button @click="handleLogout"
            class="bg-rose-500 hover:bg-rose-600 text-white px-4 py-2 rounded-xl text-sm font-semibold active:scale-95 transition-all">
            Sair
          </button>
        </div>

        <div class="md:hidden flex items-center">
          <button @click="isMenuOpen = !isMenuOpen" class="text-white hover:bg-blue-700 p-2 rounded-lg transition-colors">
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path v-if="!isMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <div v-show="isMenuOpen" class="md:hidden bg-blue-700 border-t border-blue-500 animate-in slide-in-from-top duration-300">
      <div class="px-2 pt-2 pb-3 space-y-1">
        <router-link @click="closeMenu" to="/dashboard" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800">Dashboard</router-link>
        <router-link @click="closeMenu" to="/transactions" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800">Transações</router-link>
        <router-link @click="closeMenu" to="/categories" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800">Categorias</router-link>
        <div class="border-t border-blue-600 my-2"></div>
        <button @click="showReportsModal = true; closeMenu()" class="w-full text-left text-blue-200 px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2">
          <font-awesome-icon icon="fa-solid fa-file-pdf" />
          Relatórios
        </button>
        <router-link @click="closeMenu" to="/profile" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800">Meu Perfil</router-link>
        <button @click="handleLogout" class="w-full text-left text-rose-300 px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800">Sair</button>
      </div>
    </div>

    <ReportsModal :show="showReportsModal" @close="showReportsModal = false" />
  </nav>
</template>

<style scoped>
/* O Tailwind lida com o active-class via prop do Vue Router, não precisamos mais de CSS manual aqui */
</style>