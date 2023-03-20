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

      LocalDateTime now = LocalDateTime.now();
      if (Objects.nonNull(room.getUpdatedAt()) && now.isBefore(room.getUpdatedAt().plusSeconds(5))) {
        return UpdateStatus.NOT_UPDATED;
      }

      if (Objects.isNull(room.getStatus())) {
        room.setStatus(Room.Status.START);
      } else {
        room.setStatus(room.getStatus().next());
      }
      room.setUpdatedAt(now);
      roomRepository.save(room);
      log.info("DealerTask: " + room.getId() + " " + room.getStatus());
      return UpdateStatus.UPDATED;
    });
  }
}
