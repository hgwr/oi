package jp.moreslowly.oi.tasks;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import jp.moreslowly.oi.common.RoomLimitation;
import jp.moreslowly.oi.repository.RoomRepository;
import jp.moreslowly.oi.service.CardService;

public class DealerManager {

  public enum UpdateStatus {
    UPDATED,
    NOT_UPDATED
  }

  public interface BeNotified {
    void afterUpdate();
  }

  public interface Updatable {
    UpdateStatus update();
  }

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private CardService cardService;

  private ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

  private ExecutorService runners = Executors
      .newFixedThreadPool(RoomLimitation.MAX_ROOM_SIZE);

  private Object getLock(String roomId) {
    return lockMap.computeIfAbsent(roomId, k -> new Object());
  }

  public CardService getCardService() {
    return cardService;
  }

  public void waitForUpdating(String roomId, BeNotified proc) {
    Object lock = getLock(roomId);
    synchronized (lock) {
      try {
        lock.wait();
      } catch (InterruptedException e) {
        // do nothing
      }
      proc.afterUpdate();
    }
  }

  public void updateAndNotify(String roomId, Updatable proc) {
    Object lock = getLock(roomId);
    synchronized (lock) {
      if (proc.update() == UpdateStatus.UPDATED) {
        lock.notifyAll();
      }
    }
  }

  @Scheduled(fixedRate = 1000)
  public void startDealer() {
    roomRepository.findAll().forEach(room -> {
      if (Objects.isNull(room)) {
        return;
      }
      runners.submit(new DealerTask(this, roomRepository, room.getId()));
    });
  }
}
