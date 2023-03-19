package jp.moreslowly.oi.tasks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import jp.moreslowly.oi.repository.RoomRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DealerManager {

  @Autowired
  private RoomRepository roomRepository;

  private ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

  public ConcurrentMap<String, Object> getLockMap() {
    return lockMap;
  }

  @Scheduled(fixedRate = 1000)
  public void startDealer() {
    log.info("startDealer");
    roomRepository.findAll().forEach(room -> {
      new Thread(new DealerTask(lockMap, roomRepository, room.getId())).start();
    });
  }
}
