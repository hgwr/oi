package jp.moreslowly.oi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jp.moreslowly.oi.exception.InternalErrorException;
import jp.moreslowly.oi.models.Card;

@Service
public class CardServiceImpl implements CardService {

  @Override
  public int evaluate(List<Card> cards) {
    Optional<Integer> maybeSum = cards.stream().map(card -> card.getRank().getPoint()).reduce((a, b) -> a + b);
    if (!maybeSum.isPresent()) {
      throw new InternalErrorException("some thing wrong in point calculation");
    }
    return maybeSum.get() % 10;
  }
}
