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
  resources/db/migration/    # Flyway (V1-V7)

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
HTTP_PORT=80
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
| `DELETE` | `/api/transactions/{id}/group-forward` | Excluir este e os próximos do grupo |
| `DELETE` | `/api/transactions/{id}/group` | Excluir todo o grupo |
| `GET` | `/api/categories` | Listar categorias |
| `POST` | `/api/categories` | Criar categoria |
| `GET` | `/api/reports/transactions/monthly` | PDF mensal |

---

## 🧪 Testes

```bash
cd mfc-back
./mvnw verify

cd ../mfc-front
npm ci
npm test
npm run lint -- --quiet
npm run build
```

Os testes rápidos do backend usam H2. A suíte de migrações usa PostgreSQL via Testcontainers e é ignorada automaticamente quando o Docker não está disponível.

---

## 📦 Deploy

Pull requests e pushes na `dev`/`main` executam testes, lint, build e auditoria. O deploy da `main` só começa após o CI terminar com sucesso e atualiza os serviços sem executar `docker compose down`, preservando o volume do PostgreSQL.

O backend e o frontend rodam como usuários sem privilégios nos containers. A saúde pode ser consultada em `/api/actuator/health`.

## 🔐 Segurança e sessões

- Access tokens expirados são renovados uma única vez e as requisições concorrentes aguardam a mesma renovação.
- Refresh tokens são rotacionados a cada uso e revogados no logout.
- Respostas de erro seguem `application/problem+json` e não expõem exceções internas.
- E-mails e nomes de categorias são únicos sem diferenciar maiúsculas de minúsculas.

## 📄 Relatórios

Os PDFs são gerados com Apache PDFBox (Apache License 2.0). O total de uma compra parcelada é preservado exatamente; qualquer diferença de centavos fica na última parcela.

---

## 📄 Licença

[MIT](LICENSE)
