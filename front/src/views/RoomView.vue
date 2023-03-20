<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import roomService from '../services/RoomService'
import { Room } from '../types/Room'
import { Bet } from '../types/Bet'
import CardComponent from '../components/CardComponent.vue'
import BetButton from '../components/BetButton.vue'
import BetDialog from '../components/BetDialog.vue'

const router = useRouter()
const route = useRoute()

if (!route.query.id) {
  router.push('/')
}
const roomId = route.query.id as string

let room = ref<Room>({} as Room)
let wallet = ref<number>(0)
let selectedHandIndex = ref<number>(0)
let isDisplayBetDialog = ref<boolean>(false)

const fetch = async () => {
  room.value = await roomService.enterRoom(roomId)
}
fetch()

roomService.subscribeToRoom(roomId, (newRoom: Room) => {
  if (newRoom) {
    room.value = newRoom
    wallet.value = room.value.wallets[room.value.yourName] || 0
  }
})

const bet = async (roomId: string, userName: string, handIndex: number) => {
  console.log('bet: ', roomId, userName, handIndex)
  selectedHandIndex.value = handIndex
  isDisplayBetDialog.value = true
}

const betComplete = async (roomId: string, userName: string, handIndex: number, betAmount: number) => {
  console.log('betComplete: ', roomId, userName, handIndex, betAmount)
  await roomService.bet(roomId, userName, handIndex, betAmount)
  isDisplayBetDialog.value = false
}

const closeBetDialog = () => {
  isDisplayBetDialog.value = false
}

const betsOf = (handIndex: number) => {
  if (room.value.bets) {
    return room.value.bets.filter((bet) => bet.handIndex === handIndex && bet.userName !== room.value.yourName)
  }
  return []
}

const myBetsOf = (handIndex: number) => {
  if (room.value.bets) {
    return room.value.bets.filter((bet) => bet.handIndex === handIndex && bet.userName === room.value.yourName)
  }
  return []
}

const roomHands = () => {
  let returnValue = []
  if (room.value.hands1) {
    returnValue.push(room.value.hands1)
  }
  if (room.value.hands2) {
    returnValue.push(room.value.hands2)
  }
  if (room.value.hands3) {
    returnValue.push(room.value.hands3)
  }
  if (room.value.hands4) {
    returnValue.push(room.value.hands4)
  }
  if (room.value.hands5) {
    returnValue.push(room.value.hands5)
  }
  if (room.value.hands6) {
    returnValue.push(room.value.hands6)
  }
  if (room.value.hands7) {
    returnValue.push(room.value.hands7)
  }
  return returnValue
}
</script>

<template>
  <div>{{ roomId }}</div>

  <div>あなたの名前 : {{ room.yourName }} {{ wallet }}</div>

  <div>
    {{ room.status }}
  </div>

  <div
    v-for="member in room.members"
    :key="member"
  >
    {{ member }}
  </div>

  <div class="desk">
    <div
      class="handRow"
      v-for="(hands, index) in roomHands()"
    >
      <CardComponent
        v-for="card in hands"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
      <div v-if="myBetsOf(index + 1).length > 0">
        <div
          class="betAmount"
          v-for="bet in myBetsOf(index + 1)"
          :key="bet.userName"
        >
          {{ bet.betAmount }}
        </div>
      </div>
      <div v-else>
        <BetButton
          @bet="bet"
          :roomId="roomId"
          :userName="room.yourName"
          :handIndex="index + 1"
        />
      </div>
      <div v-if="betsOf(index + 1).length > 0">
        <div
          class="betAmount"
          v-for="bet in betsOf(index + 1)"
          :key="bet.userName"
        >
          {{ bet.betAmount }}
        </div>
      </div>
    </div>
  </div>

  <BetDialog
    v-if="isDisplayBetDialog"
    :roomId="roomId"
    :userName="room.yourName"
    :handIndex="selectedHandIndex"
    @closeBetDialog="closeBetDialog"
    @bet="betComplete"
  />
</template>

<style scoped>
.handRow {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.betAmount {
  font-size: 20px;
  padding: 5px;
  border: 1px solid black;
  border-radius: 0px;
  margin: 5px;
}
</style>
