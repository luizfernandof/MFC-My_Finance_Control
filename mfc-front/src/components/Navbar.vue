<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import ReportsModal from './ReportsModal.vue';

const router = useRouter();
const isMenuOpen = ref(false);
const showReportsModal = ref(false);
const isReportsSubmenuOpen = ref(false);

function handleLogout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  router.push('/');
}

const closeMenu = () => {
  isMenuOpen.value = false;
  isReportsSubmenuOpen.value = false;
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
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all inline-flex items-center gap-2"
              active-class="bg-blue-800 text-white shadow-inner">
              <font-awesome-icon icon="fa-solid fa-chart-line" />
              Dashboard
            </router-link>
            <router-link to="/transactions"
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all inline-flex items-center gap-2"
              active-class="bg-blue-800 text-white shadow-inner">
              <font-awesome-icon icon="fa-solid fa-exchange-alt" />
              Transações
            </router-link>
            <router-link to="/categories"
              class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all inline-flex items-center gap-2"
              active-class="bg-blue-800 text-white shadow-inner">
              <font-awesome-icon icon="fa-solid fa-tags" />
              Categorias
            </router-link>
            <div class="relative" @mouseleave="isReportsSubmenuOpen = false">
              <button @mouseenter="isReportsSubmenuOpen = true"
                class="text-blue-100 hover:text-white hover:bg-blue-700 px-4 py-2 rounded-xl text-sm font-bold transition-all inline-flex items-center gap-2">
                <font-awesome-icon icon="fa-solid fa-file-alt" />
                Relatórios
                <font-awesome-icon icon="fa-solid fa-chevron-down" class="text-[10px] ml-0.5 transition-transform" :class="{ 'rotate-180': isReportsSubmenuOpen }" />
              </button>
              <div v-show="isReportsSubmenuOpen"
                class="absolute top-full left-0 pt-1 min-w-[200px] z-50"
                @mouseenter="isReportsSubmenuOpen = true">
                <div class="bg-blue-700 rounded-xl shadow-lg overflow-hidden">
                  <button @click="showReportsModal = true; isReportsSubmenuOpen = false"
                    class="w-full text-left text-blue-100 hover:text-white hover:bg-blue-800 px-4 py-3 text-sm font-bold transition-all flex items-center gap-2">
                    <font-awesome-icon icon="fa-solid fa-calendar-alt" />
                    Extrato Mensal
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="hidden md:flex items-center gap-3">
          <router-link to="/profile"
            class="text-white border border-blue-400 hover:bg-blue-500 px-4 py-2 rounded-xl text-sm font-bold transition-all inline-flex items-center gap-2">
            <font-awesome-icon icon="fa-solid fa-user" />
            Meu Perfil
          </router-link>
          <button @click="handleLogout"
            class="bg-rose-500 hover:bg-rose-600 text-white px-4 py-2 rounded-xl text-sm font-semibold active:scale-95 transition-all inline-flex items-center gap-2">
            <font-awesome-icon icon="fa-solid fa-sign-out-alt" />
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
        <router-link @click="closeMenu" to="/dashboard" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2"><font-awesome-icon icon="fa-solid fa-chart-line" /> Dashboard</router-link>
        <router-link @click="closeMenu" to="/transactions" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2"><font-awesome-icon icon="fa-solid fa-exchange-alt" /> Transações</router-link>
        <router-link @click="closeMenu" to="/categories" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2"><font-awesome-icon icon="fa-solid fa-tags" /> Categorias</router-link>
        <div class="border-t border-blue-600 my-2"></div>
        <div>
          <button @click="isReportsSubmenuOpen = !isReportsSubmenuOpen"
            class="w-full text-left text-blue-200 px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center justify-between">
            <span class="flex items-center gap-2">
              <font-awesome-icon icon="fa-solid fa-file-alt" />
              Relatórios
            </span>
            <font-awesome-icon icon="fa-solid fa-chevron-down" class="text-xs transition-transform" :class="{ 'rotate-180': isReportsSubmenuOpen }" />
          </button>
          <div v-show="isReportsSubmenuOpen" class="ml-4">
            <button @click="showReportsModal = true; closeMenu()"
              class="w-full text-left text-blue-100 px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2">
              <font-awesome-icon icon="fa-solid fa-calendar-alt" />
              Extrato Mensal
            </button>
          </div>
        </div>
        <router-link @click="closeMenu" to="/profile" class="block text-white px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2"><font-awesome-icon icon="fa-solid fa-user" /> Meu Perfil</router-link>
        <button @click="handleLogout" class="w-full text-left text-rose-300 px-4 py-3 rounded-xl text-base font-bold hover:bg-blue-800 flex items-center gap-2"><font-awesome-icon icon="fa-solid fa-sign-out-alt" /> Sair</button>
      </div>
    </div>

    <ReportsModal :show="showReportsModal" @close="showReportsModal = false" />
  </nav>
</template>

<style scoped>
/* O Tailwind lida com o active-class via prop do Vue Router, não precisamos mais de CSS manual aqui */
</style>