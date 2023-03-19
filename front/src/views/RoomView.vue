<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import roomService from '../services/RoomService'
import { Room } from '../types/Room'

const router = useRouter()
const route = useRoute()

if (!route.query.id) {
  router.push('/')
}
const roomId = route.query.id as string

let room = ref<Room>({} as Room)

const fetch = async () => {
  room.value = await roomService.getRoomById(roomId)
}
fetch()

roomService.subscribeToRoom(roomId, () => {
  fetch()
})

</script>

<template>
  <h1>Room {{ roomId }}</h1>

  <div>
    あなたの名前 : {{ room.yourName }}
  </div>

  <div>
  {{ room }}
  </div>
</template>
