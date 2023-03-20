<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import roomService from '../services/RoomService'
import { Room } from '../types/Room'
import CardComponent from '../components/CardComponent.vue'
import BetButton from '../components/BetButton.vue'

const router = useRouter()
const route = useRoute()

if (!route.query.id) {
  router.push('/')
}
const roomId = route.query.id as string

let room = ref<Room>({} as Room)
let wallet = ref<number>(0)

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
}

</script>

<template>
  <div>{{ roomId }}</div>

  <div>
    あなたの名前 : {{ room.yourName }} {{ wallet }}
  </div>

  <div>
  {{ room.status }}
  </div>

  <div v-for="member in room.members" :key="member">
    {{ member }}
  </div>

  <div>
    <div class="handRow" v-if="room.hands1">
      <CardComponent v-for="card in room.hands1" :key="card.suit + ' ' + card.rank" :card="card" />
      <BetButton @bet="bet" :roomId="roomId" :userName="room.yourName" :handIndex="1" />
    </div>
    <div>
      <CardComponent v-for="card in room.hands2" :key="card.suit + ' ' + card.rank" :card="card" />
    </div>
    <div>
      <CardComponent v-for="card in room.hands3" :key="card.suit + ' ' + card.rank" :card="card" />
    </div>
    <div>
      <CardComponent v-for="card in room.hands4" :key="card.suit + ' ' + card.rank" :card="card" />
    </div>
    <div>
      <CardComponent v-for="card in room.hands5" :key="card.suit + ' ' + card.rank" :card="card" />
    </div>
    <div>
      <CardComponent v-for="card in room.hands6" :key="card.suit + ' ' + card.rank" :card="card" />
    </div>
    <div>
      <CardComponent v-for="card in room.hands7" :key="card.suit + ' ' + card.rank" :card="card" />
    </div>
  </div>
</template>

<style scoped>
.handRow {
  display: flex;
  flex-direction: row;
  align-items: center;
}

</style>
