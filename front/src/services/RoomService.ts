import axios from '../axios'

import { Room } from '../types/Room'

export default class RoomService {
  static async enterRoom(roomId: string): Promise<Room> {
    const { data } = await axios.get(`/room/`, { params: { id: roomId } })
    return data
  }

  static async subscribeToRoom(roomId: string, callback: (room: Room) => void): Promise<Room> {
    while (true) {
      try {
        const response = await axios.get(`/room/subscribe`, { params: { id: roomId } })

        if (response.status == 502) {
          await new Promise((resolve) => setTimeout(resolve, 1000))
        } else if (response.status != 200) {
          await new Promise((resolve) => setTimeout(resolve, 1000))
        } else {
          let room = response.data
          callback(room)
          await new Promise((resolve) => setTimeout(resolve, 100))
        }
      } catch (error) {
        await new Promise((resolve) => setTimeout(resolve, 1000))
      }
    }
  }
}
