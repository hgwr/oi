package jp.moreslowly.oi.tasks;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import jp.moreslowly.oi.repository.RoomRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SweeperTask {

  private static final int EXPIRED_MINUTES = 15;
  private static final int SWEEP_FIXED_DELAY = 10 * 1000;

  @Autowired private RoomRepository roomRepository;

  @Scheduled(fixedDelay = SWEEP_FIXED_DELAY)
  public void sweep() {
    roomRepository.findAll().forEach(room -> {
      if (room.getLastAccessedAt().plusMinutes(EXPIRED_MINUTES).isBefore(LocalDateTime.now())) {
        log.info("### Sweeping room: {}", room.getId());
        roomRepository.delete(room);
      }
    });
  }
}
