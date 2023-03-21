import { AxiosError } from 'axios'
import axios from '../axios'

import { Room } from '../types/Room'

export default class RoomService {
  static async enterRoom(roomId: string): Promise<Room> {
    const { data } = await axios.post(`/room/`, { id: roomId })
    if (data.status == 503) {
      console.log('Room is full')
    }
    return data
  }

  static async subscribeToRoom(roomId: string, callback: (room: Room) => void): Promise<Room> {
    while (true) {
      try {
        const response = await axios.post(`/room/subscribe`, { id: roomId })

        if (response.status == 200) {
          let room = response.data
          callback(room)
        } else {
          console.log("not 200: ", response.status, response.data.message)
          await new Promise((resolve) => setTimeout(resolve, 1000))
        }
      } catch (error) {
        if (error instanceof AxiosError) {
          console.log("Error: ", error.response?.data.message)
        }
        await new Promise((resolve) => setTimeout(resolve, 1000))
      }
      await new Promise((resolve) => setTimeout(resolve, 500))
    }
  }

  static async bet(roomId: string, userName: string, handIndex: number, betAmount: number): Promise<Room> {
    const { data } = await axios.post(`/room/bet`, { roomId, userName, handIndex, betAmount })
    return data
  }

  static async requestCard(roomId: string, userName: string, handIndex: number): Promise<Room> {
    const { data } = await axios.post(`/room/requestCard`, { roomId, userName, handIndex })
    return data
  }
}
