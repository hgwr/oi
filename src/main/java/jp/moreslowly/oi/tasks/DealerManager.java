package jp.moreslowly.oi.tasks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import jp.moreslowly.oi.repository.RoomRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
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

  private ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

  private Object getLock(String roomId) {
    return lockMap.computeIfAbsent(roomId, k -> new Object());
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
      new Thread(new DealerTask(this, roomRepository, room.getId())).start();
    });
  }
}
