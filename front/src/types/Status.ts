export const Status = {
  START: 'START',
  SHUFFLE: 'SHUFFLE',
  HAND_OUT_CARDS: 'HAND_OUT_CARDS',
  WAIT_TO_BET: 'WAIT_TO_BET',
  WAIT_TO_REQUEST: 'WAIT_TO_REQUEST',
  DEALER_TURN: 'DEALER_TURN',
  LIQUIDATION: 'LIQUIDATION',
  END: 'END',
} as const

export type StatusType = typeof Status[keyof typeof Status];
