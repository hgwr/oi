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
  private List<Card> hands1;
  private List<Card> hands2;
  private List<Card> hands3;
  private List<Card> hands4;
  private List<Card> hands5;
  private List<Card> hands6;
  private List<Card> hands7;
  private List<BetDto> bets;
  private Status status;

  public static RoomDto fromEntity(Room room, String yourName) {
    return RoomDto.builder()
        .id(room.getId())
        .yourName(yourName)
        .members(room.getMembers())
        .hands1(room.getHands1())
        .hands2(room.getHands2())
        .hands3(room.getHands3())
        .hands4(room.getHands4())
        .hands5(room.getHands5())
        .hands6(room.getHands6())
        .hands7(room.getHands7())
        .bets(Objects.isNull(room.getBets())
            ? new ArrayList<>()
            : room.getBets().stream().map(BetDto::fromEntity).collect(Collectors.toList()))
        .status(room.getStatus())
        .build();
  }
}
