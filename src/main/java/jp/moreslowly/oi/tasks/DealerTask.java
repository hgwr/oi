package jp.moreslowly.oi.tasks;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import jp.moreslowly.oi.dao.Room;
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

  private final int GENERAL_TIMEOUT_SEC = 30;

  private UpdateStatus processStart(Room room) {
    LocalDateTime now = LocalDateTime.now();
    if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(room.getUpdatedAt().plusSeconds(GENERAL_TIMEOUT_SEC))) {
      return UpdateStatus.NOT_UPDATED;
    }

    room.setStatus(Room.Status.START.next());
    room.setUpdatedAt(now);
    roomRepository.save(room);
    return UpdateStatus.UPDATED;
  }

  private UpdateStatus processShuffle(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processHandOutCards(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processWaitToBet(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processWaitToRequest(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processDealerTurn1(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processDealerTurn2(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processLiquidation(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }

  private UpdateStatus processEnd(Room room) {
    return UpdateStatus.NOT_UPDATED;
  }
}
