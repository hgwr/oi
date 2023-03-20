package jp.moreslowly.oi.dto;

import jp.moreslowly.oi.dao.Bet;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class BetDto {
  private String roomId;
  private String userName;
  private Integer handIndex;
  private Integer betAmount;

  public static BetDto fromEntity(Bet bet) {
    return BetDto.builder()
        .roomId(bet.getRoomId())
        .userName(bet.getUserName())
        .handIndex(bet.getHandIndex())
        .betAmount(bet.getBetAmount())
        .build();
  }
}