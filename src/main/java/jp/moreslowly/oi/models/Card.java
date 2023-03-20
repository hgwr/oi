package jp.moreslowly.oi.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public final class Card implements Serializable {
  public enum Suit {
    SPADE("♠️"),
    HEART("♥️"),
    DIAMOND("♦️"),
    CLUB("♣️");
    private String symbol;
    private Suit(String symbol) {
      this.symbol = symbol;
    }
    public String toString() {
      return symbol;
    }
  }

  public enum Rank {
    ACE("A"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K");
    private String rank;
    private Rank(String rank) {
      this.rank = rank;
    }
    public String toString() {
      return rank;
    }
    public int getPoint() {
      if (this == ACE) {
        return 1;
      } else if (this == JACK) {
        return 11;
      } else if (this == QUEEN) {
        return 12;
      } else if (this == KING) {
        return 13;
      } else {
        return Integer.parseInt(rank);
      }
    }
  }

  private Suit suit;
  private Rank rank;

  public String toString() {
    return suit.toString() + rank.toString();
  }

  public static List<Card> generateCardDeck() {
    List<Card> cardDeck = new ArrayList<>();
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        cardDeck.add(new Card(suit, rank));
      }
    }
    Collections.shuffle(cardDeck);
    return cardDeck;
  }
}
