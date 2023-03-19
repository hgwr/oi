<script setup lang="ts">
import { ref } from 'vue'
import axios from '../axios'

let count = ref<string[]>([])

const fetch = async () => {
  const { data } = await axios.get('/users/count')
  count.value = data
}

fetch()
</script>

<template>
  <h1>Oi</h1>
  <div>
    ユーザ参加数： {{ count }}
  </div>
</template>
