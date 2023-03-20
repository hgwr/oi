import axios from '../axios'

import { Room } from '../types/Room'

export default class RoomService {
  static async getRoomById(roomId: string): Promise<Room> {
    const { data } = await axios.get(`/room/`, { params: { id: roomId } })
    return data
  }

  static async subscribeToRoom(roomId: string, callback: (room: Room) => void): Promise<Room> {
    while (true) {
      try {
        const response = await axios.get(`/room/subscribe`, { params: { id: roomId } })

        if (response.status == 502) {
          // timeout
          // do nothing
          await new Promise((resolve) => setTimeout(resolve, 1000))
          console.log('1')
        } else if (response.status != 200) {
          // some error occurred
          // wait 1 second and retry
          await new Promise((resolve) => setTimeout(resolve, 1000))
          console.log('2')
        } else {
          console.log('3')
          let room = response.data
          callback(room)
          await new Promise((resolve) => setTimeout(resolve, 100))
        }
      } catch (error) {
        // some error occurred
        // wait 1 second and retry
        console.log('4')
        await new Promise((resolve) => setTimeout(resolve, 1000))
      }
    }
  }
}
