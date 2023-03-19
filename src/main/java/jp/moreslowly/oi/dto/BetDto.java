package jp.moreslowly.oi.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class BetDto {
  private String roomId;
  private String userName;
  private Integer handIndex;
  private Integer betAmount;
}
