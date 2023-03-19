package jp.moreslowly.oi.dto;

import java.util.List;

import jp.moreslowly.oi.dao.Room.Status;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class RoomDto {
  private String id;
  private String yourName;
  private List<String> members;
  private List<List<String>> hands;
  private List<BetDto> bets;
  private Status status;
}
