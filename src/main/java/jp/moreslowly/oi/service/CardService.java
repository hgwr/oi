package jp.moreslowly.oi.service;

import java.util.List;

import jp.moreslowly.oi.models.Card;

public interface CardService {
  int evaluate(List<Card> cards);
}
