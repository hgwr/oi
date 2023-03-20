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
    SPADE,
    HEART,
    DIAMOND,
    CLUB
  }

  public enum Rank {
    ACE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING
  }

  private Suit suit;
  private Rank rank;

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
