<script setup>
import { computed } from 'vue';
import { Doughnut } from 'vue-chartjs';
import { 
  Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale 
} from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale);

const props = defineProps({
  chartData: {
    type: Object,
    required: true
  }
});

const chartOptions = computed(() => {
  const isSmallScreen = window.innerWidth < 768;

  return {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '72%',
    plugins: {
      legend: {
        position: 'bottom',
        // 'start' mantém o alinhamento vertical à esquerda no mobile
        align: isSmallScreen ? 'start' : 'center',
        labels: { 
          padding: 20, 
          usePointStyle: true,
          pointStyle: 'rectRounded',
          boxWidth: 8,
          textAlign: 'left',
          font: { 
            weight: 'bold', 
            family: 'Inter, sans-serif',
            size: isSmallScreen ? 11 : 13
          },
          color: '#475569'
        }
      },
      tooltip: {
        backgroundColor: '#1e293b',
        padding: 12,
        cornerRadius: 10
      }
    },
    layout: {
      padding: isSmallScreen ? { left: 20, right: 20, bottom: 20 } : 0
    }
  };
});
</script>

<template>
  <div class="chart-container">
    <Doughnut :data="props.chartData" :options="chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative; 
  height: 400px; 
  width: 100%;
}

@media (max-width: 768px) {
  .chart-container {
    height: 550px; /* Espaço para as legendas alinhadas em coluna */
  }
}
</style>