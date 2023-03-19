import axios from '../axios'

import { Room } from '../types/Room'

export default class RoomService {
  static async getRoomById(roomId: string): Promise<Room> {
    const { data } = await axios.get(`/room/`, { params: { id: roomId } })
    return data
  }
}
