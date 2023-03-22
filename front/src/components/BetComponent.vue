<script setup lang="ts">
import { Bet } from '../types/Bet'
import { namedColorSet } from '../computedStyles/namedColorSet'

const props = defineProps<{
  bet: Bet
}>()
</script>

<template>
  <div :class="['betAmount', namedColorSet(bet.userName)]">
    {{ bet.betAmount }}
    <TransitionGroup>
      <span v-if="bet.result === 'WIN'">勝ち</span>
      <span v-if="bet.result === 'LOSE'">負け</span>
      <span v-if="bet.result === 'DRAW'">引き分け</span>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.betAmount {
  font-size: 14px;
  padding: 5px;
  border: 1px solid black;
  border-radius: 0px;
  margin: 5px;
}

.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}

</style>
