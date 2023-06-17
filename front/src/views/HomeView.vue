<script setup lang="ts">
import { ref } from 'vue'
import { v4 as uuidv4 } from 'uuid';
import axios from '../axios'

let count = ref<string>('')
let roomIdList = ref<string[]>([])

const fetch = async () => {
  const countResponse = await axios.get('/users/count')
  count.value = countResponse.data
  const roomsResponse = await axios.get('/room/')
  roomIdList.value = roomsResponse.data
}

fetch()

const addRoom = async () => {
  if (roomIdList.value.length >= 10) {
    alert('ルームは10個までです')
    return
  }
  roomIdList.value.push(uuidv4())
}

</script>

<template>
  <h1>Oi</h1>
  <div class="userCount">
    ユーザ参加数： {{ count }}
  </div>
  <div class="content">
    <ul class="roomList">
      <li v-for="roomId in roomIdList" :key="roomId">
        <router-link :to="{
          name: 'room',
          query: {
            id: roomId
          }
        }">{{ roomId }}</router-link>
      </li>
    </ul>
  </div>
  <div class="footer">
    <button class="createRoomButton" @click="addRoom">ルームを作成</button>
  </div>
</template>

<style scoped>
.userCount {
  font-size: 20px;
  margin-bottom: 20px;
}
.content {
  display: flex;
  justify-content: center;
}
.roomList {
  list-style: none;
  padding: 0;
  margin: 0;
}
.roomList li {
  margin-bottom: 10px;
}
.footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.createRoomButton {
  width: 200px;
  height: 50px;
  font-size: 20px;
}

</style>
