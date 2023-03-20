package jp.moreslowly.oi.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@RedisHash("bet")
@Data
@SuperBuilder
@AllArgsConstructor
public class Bet {
  public enum Result {
    WIN("WIN"),
    LOSE("LOSE"),
    DRAW("DRAW");
    private String value;
    private Result(String value) {
      this.value = value;
    }
  }
  @Id private String id;
  private String roomId;
  private String userName;
  private Integer handIndex;
  private Integer betAmount;
  private Result result;

  public Bet() {
  }
}
