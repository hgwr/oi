import axios from '../axios'

import { Room } from '../types/Room'

export default class RoomService {
  static async enterRoom(roomId: string): Promise<Room> {
    const { data } = await axios.post(`/room/`, { id: roomId })
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
          await new Promise((resolve) => setTimeout(resolve, 1000))
        }
      } catch (error) {
        await new Promise((resolve) => setTimeout(resolve, 1000))
      }
      await new Promise((resolve) => setTimeout(resolve, 500))
    }
  }
}