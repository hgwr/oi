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
let isDisplayBetDialog = ref<boolean>(false)

const fetch = async () => {
  room.value = await roomService.enterRoom(roomId)
}
fetch()

roomService.subscribeToRoom(roomId, (newRoom: Room) => {
  if (newRoom) {
    console.log('newRoom: ', newRoom)
    room.value = newRoom
    console.log(room.value.wallets)
    wallet.value = room.value.wallets[room.value.yourName] || 0
  }
})

const bet = async (roomId: string, userName: string, handIndex: number) => {
  console.log(roomId, userName, handIndex)
  isDisplayBetDialog.value = true
}

const betComplete = async (roomId: string, userName: string, handIndex: number, betAmount: number) => {
  console.log(roomId, userName, handIndex, betAmount)
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

  <div>
    <div
      class="handRow"
      v-if="room.hands1"
    >
      <CardComponent
        v-for="card in room.hands1"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
      <div v-if="myBetsOf(1).length > 0">
        <div
          class="betAmount"
          v-for="bet in myBetsOf(1)"
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
          :handIndex="1"
        />
      </div>
      <div v-if="betsOf(1).length > 0">
        <div
          class="betAmount"
          v-for="bet in betsOf(1)"
          :key="bet.userName"
        >
          {{ bet.betAmount }}
        </div>
      </div>
    </div>
    <div>
      <CardComponent
        v-for="card in room.hands2"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
    </div>
    <div>
      <CardComponent
        v-for="card in room.hands3"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
    </div>
    <div>
      <CardComponent
        v-for="card in room.hands4"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
    </div>
    <div>
      <CardComponent
        v-for="card in room.hands5"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
    </div>
    <div>
      <CardComponent
        v-for="card in room.hands6"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
    </div>
    <div>
      <CardComponent
        v-for="card in room.hands7"
        :key="card.suit + ' ' + card.rank"
        :card="card"
      />
    </div>
  </div>

  <BetDialog
    v-if="isDisplayBetDialog"
    :roomId="roomId"
    :userName="room.yourName"
    :handIndex="1"
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
