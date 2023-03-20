package jp.moreslowly.oi.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.models.Card;
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

  public static RoomDto fromEntity(Room room, String yourName) {
    return RoomDto.builder()
        .id(room.getId())
        .yourName(yourName)
        .members(room.getMembers())
        .hands(room.getHands())
        .bets(Objects.isNull(room.getBets())
            ? new ArrayList<>()
            : room.getBets().stream().map(BetDto::fromEntity).collect(Collectors.toList()))
        .status(room.getStatus())
        .build();
  }
}
