<script setup lang="ts">
import { defineProps } from 'vue';
import { Suit, SuitType, PrintableSuit, Rank, RankType, PrintableRank, Card } from '../types/Card';

defineProps<{
  card: Card
}>()

const isRed = (suit: SuitType) => {
  return suit === Suit.DIAMOND || suit === Suit.HEART
}

</script>

<template>
  <span class="card">
    <span class="suit">
      <span :class="{ red: isRed(card.suit) }">
        {{ PrintableSuit.get(card.suit) }}
      </span>
    </span>
    <span class="rank">
      {{ PrintableRank.get(card.rank) }}
    </span>
  </span>
</template>

<style scoped>

.card {
  font-family: 'Courier New', Courier, monospace;
  font-size: 50px;
  margin: 0 10px 0 10px;
}

.suit {
  padding: 0 0 0 0;
}

.red {
  color: red;
}

.rank {
  padding: 0 0 0 5px;
}

</style>
