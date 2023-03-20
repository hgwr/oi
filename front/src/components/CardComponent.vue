<script setup lang="ts">
import { defineProps } from 'vue';
import { Suit, SuitType, PrintableSuit, Rank, RankType, PrintableRank, Card } from '../types/Card';

defineProps<{
  card: Card
}>()

const isRed = (suit: SuitType) => {
  return suit === Suit.DIAMOND || suit === Suit.HEART
}

const isTen = (rank: RankType) => {
  return rank === Rank.TEN
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
      <span :class="{ ten: isTen(card.rank) }">
        {{ PrintableRank.get(card.rank) }}
      </span>
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

.ten {
  padding-bottom: 12.5px;
  display: inline-block;
  font-size: 25px;
  -webkit-transform: scale(1, 2);
  -moz-transform: scale(1, 2);
  transform: scale(1, 2);
}

</style>
