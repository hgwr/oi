package jp.moreslowly.oi.tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import jp.moreslowly.oi.common.RoomLimitation;
import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.models.Card;
import jp.moreslowly.oi.repository.RoomRepository;
import jp.moreslowly.oi.tasks.DealerManager.UpdateStatus;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DealerTask implements Runnable {

  private DealerManager manager;
  private RoomRepository roomRepository;
  private String roomId;

  public DealerTask(DealerManager manager, RoomRepository roomRepository, String roomId) {
    this.manager = manager;
    this.roomRepository = roomRepository;
    this.roomId = roomId;
  }

  @Override
  public void run() {
    manager.updateAndNotify(roomId, () -> {
      Room room = roomRepository.findById(roomId).orElse(null);
      if (Objects.isNull(room)) {
        return UpdateStatus.NOT_UPDATED;
      }

      switch (room.getStatus()) {
        case START:
          return processStart(room);
        case SHUFFLE:
          return processShuffle(room);
        case HAND_OUT_CARDS:
          return processHandOutCards(room);
        case WAIT_TO_BET:
          return processWaitToBet(room);
        case WAIT_TO_REQUEST:
          return processWaitToRequest(room);
        case DEALER_TURN_1:
          return processDealerTurn1(room);
        case DEALER_TURN_2:
          return processDealerTurn2(room);
        case LIQUIDATION:
          return processLiquidation(room);
        case END:
          return processEnd(room);
        default:
          return UpdateStatus.NOT_UPDATED;
      }
    });
  }

  private final int SHORT_TIMEOUT_SEC = 5;
  private final int GENERAL_TIMEOUT_SEC = 30;

  private UpdateStatus processStart(Room room) {
    log.info("processStart: room id {}", room.getId());
    room.setBets(null);
    room.setHands1(null);
    room.setHands2(null);
    room.setHands3(null);
    room.setHands4(null);
    room.setHands5(null);
    room.setHands6(null);
    room.setHands7(null);
    room.setDeck(null);
    room.setStatus(Room.Status.START.next());
    room.setUpdatedAt(LocalDateTime.now());
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processShuffle(Room room) {
    log.info("processShuffle");
    LocalDateTime now = LocalDateTime.now();
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(room.getUpdatedAt().plusSeconds(SHORT_TIMEOUT_SEC))) {
      return UpdateStatus.NOT_UPDATED;
    }

    List<Card> deck = Card.generateCardDeck();
    room.setDeck(deck);
    room.setStatus(Room.Status.SHUFFLE.next());
    room.setUpdatedAt(LocalDateTime.now());
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processHandOutCards(Room room) {
    log.info("processHandOutCards");
    List<List<Card>> hands = new ArrayList<>();
    for (int i = 0; i < RoomLimitation.MAX_HAND_OUT_SIZE; i++) {
      List<Card> hand = new ArrayList<>();
      for (int j = 0; j < 2; j++) {
        hand.add(room.getDeck().remove(0));
      }
      hands.add(hand);
    }
    log.info("processHandOutCards: hands={}", hands);
    room.setHands1(hands.get(0));
    room.setHands2(hands.get(1));
    room.setHands3(hands.get(2));
    room.setHands4(hands.get(3));
    room.setHands5(hands.get(4));
    room.setHands6(hands.get(5));
    room.setHands7(hands.get(6));
    room.setStatus(Room.Status.HAND_OUT_CARDS.next());
    room.setUpdatedAt(LocalDateTime.now());
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processWaitToBet(Room room) {
    log.info("processWaitToBet");
    LocalDateTime now = LocalDateTime.now();
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(room.getUpdatedAt().plusSeconds(GENERAL_TIMEOUT_SEC))) {
      return UpdateStatus.NOT_UPDATED;
    }

    room.setStatus(Room.Status.WAIT_TO_BET.next());
    room.setUpdatedAt(LocalDateTime.now());
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processWaitToRequest(Room room) {
    log.info("processWaitToRequest");
    LocalDateTime now = LocalDateTime.now();
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(room.getUpdatedAt().plusSeconds(GENERAL_TIMEOUT_SEC))) {
      return UpdateStatus.NOT_UPDATED;
    }

    room.setStatus(Room.Status.START);
    room.setUpdatedAt(LocalDateTime.now());
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processDealerTurn1(Room room) {
    log.info("processDealerTurn1");
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processDealerTurn2(Room room) {
    log.info("processDealerTurn2");
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processLiquidation(Room room) {
    log.info("processLiquidation");
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processEnd(Room room) {
    log.info("processEnd");
    return UpdateStatus.NOT_UPDATED;
  }
}
