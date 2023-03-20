package jp.moreslowly.oi.dto;

import jp.moreslowly.oi.dao.Bet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
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

  public Bet toEntity() {
    return Bet.builder()
        .roomId(this.roomId)
        .userName(this.userName)
        .handIndex(this.handIndex)
        .betAmount(this.betAmount)
        .build();
  }
}
