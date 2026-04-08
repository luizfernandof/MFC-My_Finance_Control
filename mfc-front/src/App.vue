<script setup>

import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Navbar from './components/Navbar.vue';

const route = useRoute();
const showNavbar = computed(() => route.path !== '/');

</script>

<template>
  <div class="min-h-screen">
    <Navbar v-if="showNavbar" />
    <main class="w-full">
      <router-view />
    </main>
  </div>
</template>