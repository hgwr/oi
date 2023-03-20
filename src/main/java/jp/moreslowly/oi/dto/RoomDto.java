package jp.moreslowly.oi.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jp.moreslowly.oi.dao.Bet;
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
  private Map<String, Integer> wallets;
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
    if (room.getStatus() == Status.START) {
      return maskedFromEntity(room, yourName);
    } else if (room.getStatus() == Status.SHUFFLE) {
      return maskedFromEntity(room, yourName);
    } else if (room.getStatus() == Status.HAND_OUT_CARDS) {
      return maskedFromEntity(room, yourName);
    } else if (room.getStatus() == Status.WAIT_TO_BET) {
      return maskedWithoutMeFromEntity(room, yourName);
    } else if (room.getStatus() == Status.WAIT_TO_REQUEST) {
      return maskedWithoutMeFromEntity(room, yourName);
    } else if (room.getStatus() == Status.DEALER_TURN) {
      return maskedWithoutMeFromEntity(room, yourName);
    } else if (room.getStatus() == Status.LIQUIDATION) {
      return fullOpenFromEntity(room, yourName);
    } else if (room.getStatus() == Status.END) {
      return fullOpenFromEntity(room, yourName);
    } else {
      return fullOpenFromEntity(room, yourName);
    }
  }

  private static RoomDto fullOpenFromEntity(Room room, String yourName) {
    return RoomDto.builder()
        .id(room.getId())
        .yourName(yourName)
        .members(room.getMembers())
        .wallets(room.getWallets())
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

  private static RoomDto maskedWithoutMeFromEntity(Room room, String yourName) {
    List<Bet> bets = room.getBets();
    if (Objects.nonNull(bets)) {
      bets = bets.stream().filter(bet -> bet.getUserName().equals(yourName)).collect(Collectors.toList());
    } else {
      bets = new ArrayList<>();
    }
    List<Integer> betIndexes = bets.stream().map(Bet::getHandIndex).collect(Collectors.toList());

    RoomDto dto = RoomDto.builder()
        .id(room.getId())
        .yourName(yourName)
        .members(room.getMembers())
        .wallets(room.getWallets())
        .bets(Objects.isNull(room.getBets())
            ? new ArrayList<>()
            : room.getBets().stream().map(BetDto::fromEntity).collect(Collectors.toList()))
        .status(room.getStatus())
        .build();

    for (int handIndex : Arrays.asList(1, 2, 3, 4, 5, 6, 7)) {
      if (betIndexes.contains(handIndex)) {
        dto.setHandsAt(handIndex, room.getHandsAt(handIndex));
      } else {
        List<Card> cards = new ArrayList<>();
        if (room.getHandsAt(handIndex).size() == 3) {
          cards.add(room.getHandsAt(handIndex).get(0));
          cards.add(room.getHandsAt(handIndex).get(1));
        } else {
          cards.add(room.getHandsAt(handIndex).get(0));
        }
        dto.setHandsAt(handIndex, cards);
      }
    }

    return dto;
  }

  private static RoomDto maskedFromEntity(Room room, String yourName) {
    RoomDto dto = RoomDto.builder()
        .id(room.getId())
        .yourName(yourName)
        .members(room.getMembers())
        .wallets(room.getWallets())
        .bets(Objects.isNull(room.getBets())
            ? new ArrayList<>()
            : room.getBets().stream().map(BetDto::fromEntity).collect(Collectors.toList()))
        .status(room.getStatus())
        .build();

    for (int handIndex : Arrays.asList(1, 2, 3, 4, 5, 6, 7)) {
      List<Card> cards = new ArrayList<>();
      if (room.getHandsAt(handIndex).size() == 3) {
        cards.add(room.getHandsAt(handIndex).get(0));
        cards.add(room.getHandsAt(handIndex).get(1));
      } else {
        cards.add(room.getHandsAt(handIndex).get(0));
      }
      dto.setHandsAt(handIndex, cards);
    }

    return dto;
  }

  public void setHandsAt(int handIndex, List<Card> cards) {
    switch (handIndex) {
      case 1:
        this.hands1 = cards;
        break;
      case 2:
        this.hands2 = cards;
        break;
      case 3:
        this.hands3 = cards;
        break;
      case 4:
        this.hands4 = cards;
        break;
      case 5:
        this.hands5 = cards;
        break;
      case 6:
        this.hands6 = cards;
        break;
      case 7:
        this.hands7 = cards;
        break;
      default:
        throw new IllegalArgumentException("handIndex must be 1 to 7");
    }
  }
}
