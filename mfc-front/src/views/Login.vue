<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../services/api';
import BaseInput from '../components/BaseInput.vue';

const router = useRouter();

const isLogin = ref(true);
const email = ref('');
const password = ref('');
const loading = ref(false);
const message = ref({ text: '', type: '' });
const errors = ref({ email: '', password: '' });

function validateForm() {
  let isValid = true;
  errors.value = { email: '', password: '' };

  if (!email.value) {
    errors.value.email = 'O e-mail é obrigatório.';
    isValid = false;
  } else if (!/\S+@\S+\.\S+/.test(email.value)) {
    errors.value.email = 'Insira um e-mail válido.';
    isValid = false;
  }

  if (!password.value) {
    errors.value.password = 'A senha é obrigatória.';
    isValid = false;
  } else if (password.value.length < 6) {
    errors.value.password = 'A senha deve ter pelo menos 6 caracteres.';
    isValid = false;
  }

  return isValid;
}

async function handleSubmit() {
  if (!validateForm()) return;

  message.value = { text: '', type: '' };
  loading.value = true;

  const endpoint = isLogin.value ? '/auth/login' : '/auth/register';

  try {
    const response = await api.post(endpoint, {
      email: email.value,
      password: password.value
    });

    if (isLogin.value) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      router.push('/dashboard');
    } else {
      message.value = { text: 'Conta criada com sucesso! Faça o login.', type: 'success' };
      isLogin.value = true;
      password.value = '';
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || 'Ocorreu um erro inesperado.';
    message.value = { text: errorMsg, type: 'danger' };
  } finally {
    loading.value = false;
  }
}

function toggleMode() {
  isLogin.value = !isLogin.value;
  errors.value = { email: '', password: '' };
  message.value = { text: '', type: '' };
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-4">
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-sm border border-slate-100 p-8">

      <div class="text-center mb-6">
        <h2 class="text-2xl font-bold text-blue-600 italic uppercase">
          {{ isLogin ? 'Entrar' : 'Criar Conta' }}
        </h2>
        <p class="text-slate-400 text-xs mt-1 font-medium italic">
          My Finance Control
        </p>
      </div>

      <transition name="fade">
        <div v-if="message.text"
          :class="message.type === 'success' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' : 'bg-rose-50 text-rose-700 border-rose-100'"
          class="p-3 rounded-xl mb-4 text-xs font-medium border text-center">
          <font-awesome-icon :icon="message.type === 'success' ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-exclamation'" class="mr-1" />
          {{ message.text }}
        </div>
      </transition>

      <form @submit.prevent="handleSubmit" novalidate class="space-y-4">
        <BaseInput
          label="E-mail"
          v-model="email"
          type="email"
          placeholder="exemplo@email.com"
          :error="errors.email"
          inputmode="email"
        />

        <BaseInput
          label="Senha"
          v-model="password"
          type="password"
          placeholder="••••••••"
          :error="errors.password"
        />

        <button :disabled="loading" type="submit"
          class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-semibold rounded-xl shadow-sm transition-all active:scale-[0.98] flex items-center justify-center gap-2 py-3 text-sm mt-2">
          <font-awesome-icon v-if="loading" icon="fa-solid fa-spinner" spin />
          {{ isLogin ? 'Acessar' : 'Cadastrar' }}
        </button>
      </form>

      <div class="mt-6 text-center pt-4 border-t border-slate-100">
        <button @click="toggleMode" class="text-xs font-medium text-slate-400 hover:text-blue-600 transition-colors">
          {{ isLogin ? 'Não tem uma conta? Registre-se' : 'Já possui conta? Faça login' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
