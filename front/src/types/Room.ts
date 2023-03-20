import { Bet } from './Bet'
import { Card } from './Card'
import { Status } from './Status'

export interface Room {
  id: string
  yourName: string
  members: string[]
  hands1: Card[]
  hands2: Card[]
  hands3: Card[]
  hands4: Card[]
  hands5: Card[]
  hands6: Card[]
  hands7: Card[]
  bets: Bet[]
  status: Status
}
