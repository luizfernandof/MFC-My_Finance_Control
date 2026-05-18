<script setup>
import { Doughnut } from 'vue-chartjs';
import { 
  Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale 
} from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale);

const props = defineProps({
  chartData: {
    type: Object,
    required: true
  },
  chartOptions: {
    type: Object,
    default: () => ({})
  }
});
</script>

<template>
  <div class="chart-container">
    <Doughnut :data="props.chartData" :options="props.chartOptions" />
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
    height: 550px;
  }
}
</style>