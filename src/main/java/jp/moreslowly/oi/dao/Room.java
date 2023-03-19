package jp.moreslowly.oi.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@RedisHash("room")
@Data
@SuperBuilder
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
    END
  }
  @Id private String id;
  private List<String> members;
  private List<List<String>> hands;
  private List<Bet> bets;
  private Status status;
}
