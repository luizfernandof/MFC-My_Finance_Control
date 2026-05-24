# 💰 MFC - My Finance Control

Sistema completo de controle financeiro pessoal com dashboard interativo, gestão de transações e categorias, relatórios em PDF e suporte a tema claro/escuro.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green?logo=springboot)
![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

---

## 📸 Screenshots

### Tema Claro

![Tema Claro](./docs/images/light-theme.gif) 

### Tema Escuro

![Tema Escuro](./docs/images/dark-theme.gif)

## ✨ Funcionalidades

- **Dashboard** — Entradas, saídas e saldo com variação percentual vs. mês anterior
- **Top 3 categorias** — Maiores gastos do mês em destaque
- **Gráfico de tendência** — Barras comparando entradas vs. saídas nos últimos 6 meses
- **Doughnut de gastos** — Distribuição percentual por categoria
- **Últimas transações** — Acesso rápido direto no dashboard
- **Exportar PDF** — Relatório mensal com um clique
- **CRUD completo** — Transações com parcelamento e recorrência
- **Soft delete** — Dados excluídos são preservados, não removidos
- **JWT** — Access + refresh token com roles (USER/ADMIN)
- **Tema claro/escuro** — Alternância instantânea com persistência
- **Responsivo** — Layout adaptado para mobile e desktop
- **Migrations** — Versionamento do banco via Flyway

---

## 🏗️ Estrutura

```
mfc-back/                    # Spring Boot 3.5 (Java 17)
  auth/                      # Segurança, JWT, filtros, usuários
  config/                    # CORS, Security
  controller/                # REST controllers
  dto/                       # Records de request/response
  entity/                    # Entidades JPA
  enums/                     # Enums (TransactionType, UserRole...)
  exception/                 # Tratamento global de erros
  repository/                # Spring Data JPA
  service/                   # Lógica de negócio
  resources/db/migration/    # Flyway (V1-V6)

mfc-front/                   # Vue 3 + Vite + TailwindCSS
  components/                # BarChart, DoughnutChart, BaseInput...
  composables/               # useTheme, useBreakpoint
  services/                  # Axios com refresh token
  views/                     # Dashboard, Transactions, Categories, Login
  router/                    # Vue Router
```

---

## 🚀 Rodando

```bash
git clone https://github.com/luizfernandof/MFC-My_Finance_Control.git
cd MFC-My_Finance_Control
```

Crie o `.env` na raiz:

```env
DB_NAME=mfc_db
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
JWT_SECRET=sua_chave_base64
JWT_EXPIRATION=600000
JWT_REFRESH_EXPIRATION=604800000
```

```bash
docker compose up --build -d
```

Acesse `http://localhost`.

---

## 📡 API

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Criar conta |
| `POST` | `/api/auth/login` | Login |
| `POST` | `/api/auth/refresh` | Renovar token |
| `POST` | `/api/auth/logout` | Logout |
| `GET` | `/api/dashboard/summary` | Resumo mensal |
| `GET` | `/api/dashboard/monthly-trend` | Tendência 6 meses |
| `GET` | `/api/transactions` | Listar (paginado) |
| `POST` | `/api/transactions` | Criar |
| `PUT` | `/api/transactions/{id}` | Editar |
| `DELETE` | `/api/transactions/{id}` | Excluir |
| `GET` | `/api/categories` | Listar categorias |
| `POST` | `/api/categories` | Criar categoria |
| `GET` | `/api/reports/transactions/monthly` | PDF mensal |

---

## 🧪 Testes

```bash
cd mfc-back && ./mvnw test
```

Usa H2 em memória com profile `test`.

---

## 📦 Deploy

Deploy automático via GitHub Actions ao push na `main`.

---

## 📄 Licença

[MIT](LICENSE)