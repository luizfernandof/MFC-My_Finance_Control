<script setup>
import { Bar } from 'vue-chartjs';
import {
  Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale
} from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

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
    <Bar :data="props.chartData" :options="props.chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  height: 300px;
  width: 100%;
}

@media (max-width: 768px) {
  .chart-container {
    height: 280px;
  }
}
</style>