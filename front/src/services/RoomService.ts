import axios from '../axios'

import { Room } from '../types/Room'

export default class RoomService {
  static async getRoomById(roomId: string): Promise<Room> {
    const { data } = await axios.get(`/room/`, { params: { id: roomId } })
    return data
  }

  static async subscribeToRoom(roomId: string, callback: () => void): Promise<string> {
    while (true) {
      const response = await axios.get(`/room/subscribe`, { params: { id: roomId } })

      if (response.status == 502) {
        // timeout
        // do nothing
      } else if (response.status != 200) {
        // some error occurred
        // wait 1 second and retry
        let { message } = response.data
        console.log(message)
        await new Promise((resolve) => setTimeout(resolve, 1000))
      } else {
        let { message } = response.data
        console.log(message)
        callback()
      }
    }
  }
}
