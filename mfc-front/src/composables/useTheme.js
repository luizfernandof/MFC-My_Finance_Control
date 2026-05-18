import { ref } from 'vue';

const isDark = ref(false);
let initialized = false;

function getInitialTheme() {
  const stored = localStorage.getItem('theme');
  if (stored === 'dark') return true;
  if (stored === 'light') return false;
  return false;
}

function applyTheme(dark) {
  if (dark) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
  localStorage.setItem('theme', dark ? 'dark' : 'light');
}

export function useTheme() {
  if (!initialized) {
    isDark.value = getInitialTheme();
    initialized = true;
  }
  applyTheme(isDark.value);

  function toggleTheme() {
    isDark.value = !isDark.value;
    applyTheme(isDark.value);
  }

  return { isDark, toggleTheme };
}