package jp.moreslowly.oi.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import jp.moreslowly.oi.models.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@RedisHash("room")
@Data
@SuperBuilder
@AllArgsConstructor
public class Room {
  public enum Status {
    START,
    SHUFFLE,
    HAND_OUT_CARDS,
    WAIT_TO_BET,
    WAIT_TO_REQUEST,
    DEALER_TURN_1,
    DEALER_TURN_2,
    LIQUIDATION,
    END;
    private static final Status[] values = values();
    public Status prev() {
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
    public Status next() {
        return values[(this.ordinal() + 1) % values.length];
    }
  }

  @Id private String id;
  private List<String> members;
  private List<Card> deck;
  private List<List<Card>> hands;
  private List<Bet> bets;
  private Status status;
  private LocalDateTime updatedAt;

  public Room() {
    this.status = Status.START;
  }
}
