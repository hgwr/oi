import { Bet } from './Bet'
import { Status } from './Status'

export interface Room {
  id: string
  yourName: string
  members: string[]
  hands: string[][]
  bets: Bet[]
  status: Status
}
