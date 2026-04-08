import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import Categories from '../views/Categories.vue';
import Transactions from '../views/Transactions.vue';

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/categories',
    name: 'Categories',
    component: Categories
  },
  {
    path: '/transactions',
    name: 'Transactions',
    component:  Transactions
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;