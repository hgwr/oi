export const Suit = {
  SPADE: 'SPADE',
  HEART: 'HEART',
  DIAMOND: 'DIAMOND',
  CLUB: 'CLUB',
} as const

export type SuitType = typeof Suit[keyof typeof Suit];

export const PrintableSuit: Map<SuitType, string> = new Map([
  [Suit.SPADE, '♠'],
  [Suit.HEART, '♥'],
  [Suit.DIAMOND, '♦'],
  [Suit.CLUB, '♣'],
])

export const Rank = {
  ACE: 'ACE',
  TWO: 'TWO',
  THREE: 'THREE',
  FOUR: 'FOUR',
  FIVE: 'FIVE',
  SIX: 'SIX',
  SEVEN: 'SEVEN',
  EIGHT: 'EIGHT',
  NINE: 'NINE',
  TEN: 'TEN',
  JACK: 'JACK',
  QUEEN: 'QUEEN',
  KING: 'KING',
} as const

export type RankType = typeof Rank[keyof typeof Rank];

export const PrintableRank: Map<RankType, string> = new Map([
  [Rank.ACE, 'A'],
  [Rank.TWO, '2'],
  [Rank.THREE, '3'],
  [Rank.FOUR, '4'],
  [Rank.FIVE, '5'],
  [Rank.SIX, '6'],
  [Rank.SEVEN, '7'],
  [Rank.EIGHT, '8'],
  [Rank.NINE, '9'],
  [Rank.TEN, '10'],
  [Rank.JACK, 'J'],
  [Rank.QUEEN, 'Q'],
  [Rank.KING, 'K'],
])

export interface Card {
  suit: SuitType
  rank: RankType
}
