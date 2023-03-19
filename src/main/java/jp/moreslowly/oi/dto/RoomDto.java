package jp.moreslowly.oi.dto;

import jp.moreslowly.oi.dao.Room.Status;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class RoomDto {
  private String id;
  private Status status;
}
