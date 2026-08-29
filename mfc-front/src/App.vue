<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Navbar from './components/Navbar.vue';
import { useTheme } from './composables/useTheme';

useTheme();

const route = useRoute();
const showNavbar = computed(() => Boolean(route.meta.requiresAuth));
</script>

<template>
  <div class="min-h-screen">
    <Navbar v-if="showNavbar" />
    <main class="w-full">
      <router-view />
    </main>
  </div>
</template>
