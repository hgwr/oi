package jp.moreslowly.oi.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import jp.moreslowly.oi.models.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    DEALER_TURN,
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
  @Builder.Default private List<String> members = new ArrayList<>();
  @Builder.Default private Map<String, Integer> wallets = new HashMap<>();
  @Builder.Default private List<Card> deck = new ArrayList<>();
  @Builder.Default private List<Card> hands1 = new ArrayList<>();
  @Builder.Default private List<Card> hands2 = new ArrayList<>();
  @Builder.Default private List<Card> hands3 = new ArrayList<>();
  @Builder.Default private List<Card> hands4 = new ArrayList<>();
  @Builder.Default private List<Card> hands5 = new ArrayList<>();
  @Builder.Default private List<Card> hands6 = new ArrayList<>();
  @Builder.Default private List<Card> hands7 = new ArrayList<>();
  @Builder.Default private List<Bet> bets = new ArrayList<>();
  private Status status;
  private LocalDateTime updatedAt;

  public Room() {
    this.status = Status.START;
  }

  public void reset() {
    this.deck = new ArrayList<>();
    this.members = new ArrayList<>();
    this.hands1 = new ArrayList<>();
    this.hands2 = new ArrayList<>();
    this.hands3 = new ArrayList<>();
    this.hands4 = new ArrayList<>();
    this.hands5 = new ArrayList<>();
    this.hands6 = new ArrayList<>();
    this.hands7 = new ArrayList<>();
    this.bets = new ArrayList<>();
    this.status = Status.START;
  }
}
