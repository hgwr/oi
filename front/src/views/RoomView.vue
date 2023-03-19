<script setup lang="ts">
import { ref } from 'vue'
import axios from '../axios'

let rooms = ref<string[]>([])

const fetch = async () => {
  const { data } = await axios.get('/rooms/')
  rooms.value = data
}

fetch()
</script>

<template>
  <h1>Oi</h1>
  <div v-for="room in rooms">
    {{ room }}
  </div>
</template>
