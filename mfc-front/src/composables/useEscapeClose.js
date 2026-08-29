import { onBeforeUnmount, onMounted, unref } from 'vue';

export function useEscapeClose(isOpen, close) {
  const handleKeydown = (event) => {
    if (event.key === 'Escape' && unref(isOpen)) {
      close();
    }
  };

  onMounted(() => window.addEventListener('keydown', handleKeydown));
  onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown));
}
