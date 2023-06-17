<script setup lang="ts">
import { computed } from 'vue'
import type { CSSProperties } from 'vue'

interface PieChartProps {
  percentage: number
  color: string
  size: number
}

const props = defineProps<PieChartProps>()

const chartStyle = computed((): CSSProperties => {
  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    position: 'relative',
  }
})

const backgroundStyle = computed((): CSSProperties => {
  return {
    borderRadius: '50%',
    backgroundColor: '#efefef',
    width: '100%',
    height: '100%',
    position: 'absolute',
  }
})

const foregroundStyle = computed((): CSSProperties => {
  return {
    width: '100%',
    height: '100%',
    position: 'absolute',
    background: `conic-gradient(${props.color} 0%, ${props.color} ${props.percentage}%, transparent ${props.percentage}%)`,
    clipPath: props.percentage <= 50 ? 'polygon(50% 0%, 100% 0%, 100% 100%, 0% 100%, 0% 0%)' : 'none',
  }
})
</script>

<template>
  <div
  class="pie-chart"
    v-bind:style="chartStyle"
  >
    <div
      class="pie-chart__background"
      v-bind:style="backgroundStyle"
    >
    </div>
    <div
      class="pie-chart__foreground"
      v-bind:style="foregroundStyle"
    >
    </div>
  </div>
</template>

<style scoped>
.pie-chart {
  display: inline-block;
  overflow: hidden;
  border-radius: 50%;
}
</style>
