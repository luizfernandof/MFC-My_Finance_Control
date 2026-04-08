import axios from 'axios';
import router from '../router';

const api = axios.create({
  baseURL: 'https://api.mfc.devl.com.br',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Variáveis de controle para a "Fila de Espera"
let isRefreshing = false;
let failedQueue = [];

// Função que processa a fila de requisições que ficaram aguardando o novo token
const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// 1. Interceptor de Requisição (Injeta o Token Atual)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 2. Interceptor de Resposta (A "Mágica" do Refresh Silencioso)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Verifica se o erro é 401 (Não autorizado) e se não é uma repetição
    if (error.response?.status === 401 && !originalRequest._retry) {

      // Se já estivermos renovando o token, colocamos esta requisição na fila
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      // Marcamos que o processo de refresh começou
      originalRequest._retry = true;
      isRefreshing = true;

      return new Promise((resolve, reject) => {
        const refreshToken = localStorage.getItem('refreshToken');

        // Usamos o axios puro (sem interceptors) para buscar o novo token
        axios.post('https://api.mfc.devl.com.br/auth/refresh', { refreshToken })
          .then(({ data }) => {
            // 1. Salva os novos dados
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);

            // 2. Atualiza o header padrão da nossa instância
            api.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`;
            originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;

            // 3. Libera todo mundo que estava na fila com o novo token
            processQueue(null, data.accessToken);

            // 4. Resolve a requisição original que disparou o erro
            resolve(api(originalRequest));
          })
          .catch((err) => {
            // Se o Refresh Token também falhou, limpa tudo e desloga
            processQueue(err, null);
            localStorage.clear();
            router.push('/');
            reject(err);
          })
          .finally(() => {
            isRefreshing = false;
          });
      });
    }

    // Se for qualquer outro erro (500, 404, 400), apenas repassa para o catch do componente
    return Promise.reject(error);
  }
);

export default api;
