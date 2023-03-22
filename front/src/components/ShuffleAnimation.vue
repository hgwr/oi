<template>
  <div class="card-container">
    <div
      v-for="(card, index) in shuffledCards"
      :key="index"
      class="card"
      :style="{ transform: `translate(${card.x}px, ${card.y}px)` }"
    >
      <div class="card-back"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

interface CardPosition {
  id: number;
  x: number;
  y: number;
}

function shuffleCards(cards: number[]): CardPosition[] {
  const shuffledCards: CardPosition[] = [];

  for (const card of cards) {
    const x = Math.random() * 350;
    const y = Math.random() * 220;

    shuffledCards.push({ id: card, x, y });
  }

  return shuffledCards;
}

const totalCards = 52;
const cards = Array.from({ length: totalCards }, (_, i) => i + 1);
const shuffledCards = ref<CardPosition[]>([]);

onMounted(() => {
  shuffle();
});

function shuffle() {
  shuffledCards.value = shuffleCards(cards);
}

watch(shuffledCards, () => {
  setTimeout(() => {
    shuffle();
  }, 200);
});
</script>

<style scoped>
.card-container {
  position: relative;
  width: 400px;
  height: 300px;
  overflow: hidden;
}

.card {
  position: absolute;
  width: 50px;
  height: 80px;
  background-color: transparent;
  transition: transform 0.5s ease-out;
}

.card-back {
  width: 100%;
  height: 100%;
  background-color: blue;
  border-radius: 5px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
}
</style>
