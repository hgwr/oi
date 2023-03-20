<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import roomService from '../services/RoomService'
import { Room } from '../types/Room'
import CardComponent from '../components/CardComponent.vue'

const router = useRouter()
const route = useRoute()

if (!route.query.id) {
  router.push('/')
}
const roomId = route.query.id as string

let room = ref<Room>({} as Room)

const fetch = async () => {
  room.value = await roomService.enterRoom(roomId)
}
fetch()

roomService.subscribeToRoom(roomId, (newRoom: Room) => {
  console.log('newRoom: ', newRoom)
  if (newRoom) {
    room.value = newRoom
  }
})

</script>

<template>
  <div>Room {{ roomId }}</div>

  <div>
    あなたの名前 : {{ room.yourName }}
  </div>

  <div>
  {{ room.status }}
  </div>

  <div v-for="member in room.members" :key="member">
    {{ member }}
  </div>

  <div>
    <div>
      <CardComponent v-for="card in room.hands1" :key="card.suit + ' ' + card.rank" :card="card" />
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
