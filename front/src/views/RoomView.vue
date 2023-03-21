<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import roomService from '../services/RoomService'
import { Status } from '../types/Status'
import { Room } from '../types/Room'
import CardComponent from '../components/CardComponent.vue'
import BetButton from '../components/BetButton.vue'
import BetDialog from '../components/BetDialog.vue'
import { namedColorSet } from '../computedStyles/namedColorSet'

const router = useRouter()
const route = useRoute()

if (!route.query.id) {
  router.push('/')
}
const roomId = route.query.id as string

let room = ref<Room>({} as Room)
let selectedHandIndex = ref<number>(0)
let isDisplayBetDialog = ref<boolean>(false)
let timeLeft = ref<number>(0)

onMounted(() => {
  roomService.subscribeToRoom(roomId, (newRoom: Room) => {
    if (newRoom) {
      room.value = newRoom
      timeLeft.value = room.value.timeLeft
    }
  })

  const timer = setInterval(() => {
    if (timeLeft.value <= 0) {
      return
    }
    timeLeft.value = timeLeft.value - 1
  }, 1000)

  onBeforeUnmount(() => {
    clearInterval(timer)
    roomService.stopSubscribe()
  })
})

const fetch = async () => {
  room.value = await roomService.enterRoom(roomId)
  timeLeft.value = room.value.timeLeft
}
fetch()

const walletByUserName = (userName: string): number => {
  return (room.value.wallets || {})[userName] || 0
}

interface ScoreBoard {
  userName: string
  amount: number
}

const scoreBoard = (): ScoreBoard[] => {
  const board = (room.value.members || []).map((userName) => {
    return {
      userName,
      amount: walletByUserName(userName),
    }
  })
  return board.sort((a, b) => b.amount - a.amount)
}

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

const requestCard = async (handIndex: number) => {
  console.log('requestCard: ', handIndex)
  await roomService.requestCard(roomId, room.value.yourName, handIndex)
}

const isJoined = computed(() => {
  return room.value.members && room.value.members.includes(room.value.yourName)
})
</script>

<template>
  <div class="roomId">{{ roomId }}</div>

  <div :class="['yourName', namedColorSet(room.yourName)]">
    <span> あなたの名前 : {{ room.yourName }} </span>
    <span>
      {{ walletByUserName(room.yourName) }}
    </span>
  </div>

  <div class="statusBar">
    <span>ステータス：</span>
    <span v-if="!isJoined">（観戦中）</span>
    <span v-if="room.status === Status.START">ゲーム開始</span>
    <span v-if="room.status === Status.SHUFFLE">シャッフル中</span>
    <span v-if="room.status === Status.HAND_OUT_CARDS">配布中</span>
    <span v-if="room.status === Status.WAIT_TO_BET">賭けてください</span>
    <span v-if="room.status === Status.WAIT_TO_REQUEST">もう一枚引きますか？</span>
    <span v-if="room.status === Status.DEALER_TURN">親の番</span>
    <span v-if="room.status === Status.LIQUIDATION">精算中</span>
    <span v-if="room.status === Status.END">ゲーム終了</span>
    <span>（{{ timeLeft }}）</span>
  </div>

  <div class="boardAndDesk">
    <table class="balance">
      <thead>
        <tr>
          <th>ユーザー名</th>
          <th>金額</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="row in scoreBoard()"
          :key="row.userName"
        >
          <td>
            <span :class="[namedColorSet(row.userName)]">
              {{ row.userName }}
            </span>
          </td>
          <td class="amount">{{ row.amount }}</td>
        </tr>
      </tbody>
    </table>

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
        <span v-if="index == 6 && hands.length > 0">親の手札</span>
        <template v-if="myBetsOf(index + 1).length > 0">
          <div
            :class="['myBetAmount', namedColorSet(room.yourName)]"
            v-for="bet in myBetsOf(index + 1)"
            :key="bet.userName"
          >
            {{ bet.betAmount }}
            <span v-if="bet.result === 'WIN'">勝ち</span>
            <span v-if="bet.result === 'LOSE'">負け</span>
            <span v-if="bet.result === 'DRAW'">引き分け</span>
          </div>
          <button
            class="requestCard"
            v-if="room.status === Status.WAIT_TO_REQUEST && roomHands()[index].length === 2"
            @click="requestCard(index + 1)"
          >
            もう一枚
          </button>
        </template>
        <template v-else>
          <template v-if="index + 1 != 7 && isJoined">
            <BetButton
              v-if="room.status === Status.WAIT_TO_BET"
              @bet="bet"
              :roomId="roomId"
              :userName="room.yourName"
              :handIndex="index + 1"
            />
          </template>
        </template>
        <template v-if="betsOf(index + 1).length > 0">
          <div
            v-for="bet in betsOf(index + 1)"
            :key="bet.userName"
            :class="['betAmount', namedColorSet(bet.userName)]"
          >
            {{ bet.betAmount }}
            <span v-if="bet.result === 'WIN'">勝ち</span>
            <span v-if="bet.result === 'LOSE'">負け</span>
            <span v-if="bet.result === 'DRAW'">引き分け</span>
          </div>
        </template>
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
.roomId {
  font-size: 10px;
  padding: 3px;
  text-align: center;
}

.yourName {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  padding: 5px;
  border: 1px solid black;
  border-radius: 0px;
  margin: 5px;
}

.balance {
  font-size: 16px;
  padding: 5px;
  margin: 5px;
  border-collapse: collapse;
}

.balance th,
.balance td {
  border: 1px solid black;
  border-spacing: 0;
  padding: 2px;
}

.balance .amount {
  text-align: right;
}

.handRow {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-basis: min-content;
  margin: 5px;
}

.myBetAmount {
  font-size: 14px;
  padding: 5px;
  border: 1px solid black;
  border-radius: 0px;
  margin: 5px;
}

.betAmount {
  font-size: 14px;
  padding: 5px;
  border: 1px solid black;
  border-radius: 0px;
  margin: 5px;
}

.requestCard {
  font-size: 24px;
  padding: 5px 10px;
  border-radius: 10px;
  border: 1px solid #ccc;
  background-color: #fff;
  cursor: pointer;
  margin-left: 10px;
}

.statusBar {
  font-size: 16px;
  padding: 5px;
  border: 1px solid black;
  border-radius: 0px;
  margin: 5px;
}

@media screen and (max-width: 600px) {
  .boardAndDesk {
    display: flex;
    flex-direction: column;
    align-items: left;
    justify-content: space-evenly;
  }

  .requestCard {
    font-size: 16px;
  }
}
@media screen and (min-width: 601px) {
  .boardAndDesk {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
  }
  .desk {
    margin-left: 50px;
  }
}
</style>
