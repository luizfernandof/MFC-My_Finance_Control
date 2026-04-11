import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'

// 1. Importações do FontAwesome
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// 2. Importação de todos os ícones que usamos (Solid)
import {
  faPlus,
  faRepeat,
  faPenToSquare,
  faTrashCan,
  faXmark,
  faCheck,
  faChevronDown,
  faTriangleExclamation,
  faCircleCheck,
  faCircleExclamation,
  faSpinner,
  faArrowLeft,
  faArrowRight,
  faChevronLeft,
  faChevronRight,
  faFilePdf,
  faChartLine,
  faExchangeAlt,
  faTags,
  faFileAlt,
  faUser,
  faSignOutAlt,
  faCalendarAlt
} from '@fortawesome/free-solid-svg-icons'

// 3. Adicionando à biblioteca global
library.add(
  faPlus,
  faRepeat,
  faPenToSquare,
  faTrashCan,
  faXmark,
  faCheck,
  faChevronDown,
  faTriangleExclamation,
  faCircleCheck,
  faCircleExclamation,
  faSpinner,
  faArrowLeft,
  faArrowRight,
  faChevronLeft,
  faChevronRight,
  faFilePdf,
  faChartLine,
  faExchangeAlt,
  faTags,
  faFileAlt,
  faUser,
  faSignOutAlt,
  faCalendarAlt
)

const app = createApp(App)

// 4. Registrar o componente globalmente ANTES do router/mount
app.component('font-awesome-icon', FontAwesomeIcon)

app.use(router)
app.mount('#app')