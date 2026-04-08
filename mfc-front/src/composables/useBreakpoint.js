import { ref, onMounted, onUnmounted } from 'vue';

export function useBreakpoint() {
  const isMobile = ref(false);

  const update = () => {
    isMobile.value = window.innerWidth < 768;
  };

  onMounted(() => {
    update();
    window.addEventListener('resize', update);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', update);
  });

  return { isMobile };
}