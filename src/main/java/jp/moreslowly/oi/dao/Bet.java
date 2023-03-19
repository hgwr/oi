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
  @Id private String id;
  private String roomId;
  private String userName;
  private Integer handIndex;
  private Integer betAmount;

  public Bet() {
  }
}
