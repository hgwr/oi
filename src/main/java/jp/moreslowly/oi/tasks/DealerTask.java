package jp.moreslowly.oi.tasks;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import org.springframework.util.CollectionUtils;

import jp.moreslowly.oi.common.RoomLimitation;
import jp.moreslowly.oi.dao.Bet;
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
      Optional<Room> maybeRoom = roomRepository.findById(roomId);
      if (!maybeRoom.isPresent()) {
        return UpdateStatus.NOT_UPDATED;
      }
      Room room = maybeRoom.get();

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
        case DEALER_TURN:
          return processDealerTurn(room);
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
  private final int LONG_TIMEOUT_SEC = 600;

  private UpdateStatus processStart(Room room) {
    log.info("processStart: room id {}", room.getId());
    room.reset();
    room.setStatus(Room.Status.START.next());
    room.setUpdatedAt(LocalDateTime.now());
    room.setTimeLeft((long) SHORT_TIMEOUT_SEC);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processShuffle(Room room) {
    log.info("processShuffle");
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeLimit = room.getUpdatedAt().plusSeconds(SHORT_TIMEOUT_SEC);
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(timeLimit)) {
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

    room.setHands1(hands.get(0));
    room.setHands2(hands.get(1));
    room.setHands3(hands.get(2));
    room.setHands4(hands.get(3));
    room.setHands5(hands.get(4));
    room.setHands6(hands.get(5));
    room.setHands7(hands.get(6));
    room.setStatus(Room.Status.HAND_OUT_CARDS.next());
    room.setUpdatedAt(LocalDateTime.now());
    room.setTimeLeft((long) GENERAL_TIMEOUT_SEC);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processWaitToBet(Room room) {
    log.info("processWaitToBet");
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeLimit = room.getUpdatedAt().plusSeconds(GENERAL_TIMEOUT_SEC);
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(timeLimit)) {
      return UpdateStatus.NOT_UPDATED;
    }

    room.setStatus(Room.Status.WAIT_TO_BET.next());
    room.setUpdatedAt(LocalDateTime.now());
    room.setTimeLeft((long) GENERAL_TIMEOUT_SEC);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processWaitToRequest(Room room) {
    log.info("processWaitToRequest");

    if (CollectionUtils.isEmpty(room.getBets())) {
      room.setStatus(Room.Status.WAIT_TO_REQUEST.next());
      room.setUpdatedAt(LocalDateTime.now());
      room.setTimeLeft((long) SHORT_TIMEOUT_SEC);
      roomRepository.save(room);
      return UpdateStatus.UPDATED;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeLimit = room.getUpdatedAt().plusSeconds(GENERAL_TIMEOUT_SEC);
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(timeLimit)) {
      return UpdateStatus.NOT_UPDATED;
    }

    room.setStatus(Room.Status.WAIT_TO_REQUEST.next());
    room.setUpdatedAt(LocalDateTime.now());
    room.setTimeLeft((long) SHORT_TIMEOUT_SEC);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processDealerTurn(Room room) {
    log.info("processDealerTurn");
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeLimit = room.getUpdatedAt().plusSeconds(SHORT_TIMEOUT_SEC);
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(timeLimit)) {
      return UpdateStatus.NOT_UPDATED;
    }

    List<Card> parentCards = room.getHands7();
    int point = manager.getCardService().evaluate(parentCards);
    int threshold = Math.random() < 0.5 ? 5 : 6;
    if (point <= threshold) {
      parentCards.add(room.getDeck().remove(0));
      room.setHands7(parentCards);
    }

    room.setStatus(Room.Status.DEALER_TURN.next());
    room.setUpdatedAt(LocalDateTime.now());
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processLiquidation(Room room) {
    log.info("processLiquidation");

    List<Card> parentCards = room.getHands7();
    int parentPoint = manager.getCardService().evaluate(parentCards);
    List<Bet> bets = room.getBets();
    if (!CollectionUtils.isEmpty(bets)) {
      room.getBets().stream().forEach(bet -> {
        List<Card> hands = room.getHandsAt(bet.getHandIndex());
        int point = manager.getCardService().evaluate(hands);
        if (point > parentPoint) {
          bet.setResult(Bet.Result.WIN);
          int betAmount = bet.getBetAmount();
          int wallet = room.getWallets().get(bet.getUserName());
          room.getWallets().put(bet.getUserName(), wallet + betAmount * 2);
        } else if (point == parentPoint) {
          bet.setResult(Bet.Result.DRAW);
          int betAmount = bet.getBetAmount();
          int wallet = room.getWallets().get(bet.getUserName());
          room.getWallets().put(bet.getUserName(), wallet + betAmount);
        } else {
          bet.setResult(Bet.Result.LOSE);
        }
      });
    }

    room.setStatus(Room.Status.LIQUIDATION.next());
    room.setUpdatedAt(LocalDateTime.now());
    int timeout = GENERAL_TIMEOUT_SEC;
    if (CollectionUtils.isEmpty(room.getBets())) {
      timeout = SHORT_TIMEOUT_SEC;
    }
    room.setTimeLeft((long) timeout);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processEnd(Room room) {
    log.info("processEnd");

    int timeout = GENERAL_TIMEOUT_SEC;
    if (CollectionUtils.isEmpty(room.getBets())) {
      timeout = SHORT_TIMEOUT_SEC;
    }
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timeLimit = room.getUpdatedAt().plusSeconds(timeout);
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(timeLimit)) {
      return UpdateStatus.NOT_UPDATED;
    }

    room.reset();
    room.setStatus(Room.Status.END.next());
    room.setUpdatedAt(LocalDateTime.now());
    room.setTimeLeft(0L);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }
}
