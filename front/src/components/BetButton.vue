<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';

const props = defineProps<{
  roomId: string
  userName: string
  handIndex: number
}>()

const emit = defineEmits<{
  (e: 'bet', roomId: string, userName: string, handIndex: number): void
}>()

const bet = () => {
  emit('bet', props.roomId, props.userName, props.handIndex)
}

</script>

<template>
  <button @click="bet" class="betButton">賭ける</button>
</template>

<style scoped>

.betButton {
  font-size: 24px;
  padding: 5px 10px;
  border-radius: 10px;
  border: 1px solid #ccc;
  background-color: #fff;
  cursor: pointer;
  margin-left: 10px;
}

@media screen and (max-width: 600px) {
  .betButton {
    font-size: 16px;
  }
}

</style>