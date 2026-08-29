import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';

const routes = [
  {
	    path: '/',
	    name: 'Login',
	    component: Login,
	    meta: { guestOnly: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
	    component: () => import('../views/Dashboard.vue'),
	    meta: { requiresAuth: true }
  },
  {
    path: '/categories',
    name: 'Categories',
	    component: () => import('../views/Categories.vue'),
	    meta: { requiresAuth: true }
  },
  {
    path: '/transactions',
    name: 'Transactions',
	    component: () => import('../views/Transactions.vue'),
	    meta: { requiresAuth: true }
	  },
	  {
	    path: '/:pathMatch(.*)*',
	    name: 'NotFound',
	    component: () => import('../views/NotFound.vue')
	  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const hasSession = Boolean(localStorage.getItem('accessToken') && localStorage.getItem('refreshToken'));
  if (to.meta.requiresAuth && !hasSession) return { name: 'Login' };
  if (to.meta.guestOnly && hasSession) return { name: 'Dashboard' };
  return true;
});

export default router;
