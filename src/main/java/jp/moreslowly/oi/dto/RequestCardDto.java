package jp.moreslowly.oi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCardDto {
  private String roomId;
  private String userName;
  private Integer handIndex;
}
