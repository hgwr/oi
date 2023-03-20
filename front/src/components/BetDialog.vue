<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

const props = defineProps<{
  roomId: string
  userName: string
  handIndex: number
}>()

const emit = defineEmits<{
  (e: 'bet', roomId: string, userName: string, handIndex: number, betAmount: number): void
  (e: 'closeBetDialog'): void
}>()

const closeBetDialog = () => {
  emit('closeBetDialog')
}

const bet100 = () => {
  emit('bet', props.roomId, props.userName, props.handIndex, 100)
}

const bet500 = () => {
  emit('bet', props.roomId, props.userName, props.handIndex, 500)
}

const bet1000 = () => {
  emit('bet', props.roomId, props.userName, props.handIndex, 1000)
}
</script>

<template>
  <div class="betDialog">
    <div class="betDialog__content">
      <div class="betDialog__content__title">賭ける</div>
      <div class="betDialog__content__body">
        <div class="betDialog__content__body__row">
          <button
            class="betDialog__content__body__row__button"
            @click="bet100"
          >
            100
          </button>
        </div>
        <div class="betDialog__content__body__row">
          <button
            class="betDialog__content__body__row__button"
            @click="bet500"
          >
            500
          </button>
        </div>
        <div class="betDialog__content__body__row">
          <button
            class="betDialog__content__body__row__button"
            @click="bet1000"
          >
            1000
          </button>
        </div>
      </div>
      <div class="betDialog__content__footer">
        <button
          class="betDialog__content__footer__button"
          @click="closeBetDialog"
        >
          キャンセル
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.betDialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.betDialog__content {
  width: 300px;
  height: 300px;
  background-color: white;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
}

.betDialog__content__title {
  height: 50px;
  line-height: 50px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}

.betDialog__content__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.betDialog__content__body__row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

.betDialog__content__body__row__label {
  font-size: 20px;
  font-weight: bold;
  margin-right: 10px;
}

.betDialog__content__footer {
  height: 80px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.betDialog__content__footer__button {
  width: 150px;
  height: 40px;
  border-radius: 5px;
  border: 1px solid #ccc;
  margin-right: 10px;
  font-size: 20px;
  font-weight: bold;
  background-color: white;
}

.betDialog__content__footer__button:last-child {
  margin-right: 10px;
}

.betDialog__content__body__row__button {
  width: 150px;
  height: 40px;
  border-radius: 5px;
  border: 1px solid #ccc;
  margin-right: 10px;
  font-size: 20px;
  font-weight: bold;
  background-color: white;
}
</style>
