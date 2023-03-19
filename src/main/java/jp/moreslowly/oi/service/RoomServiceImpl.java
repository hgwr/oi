package jp.moreslowly.oi.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.common.Nickname;
import jp.moreslowly.oi.common.SessionKey;
import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.repository.RoomRepository;
import jp.moreslowly.oi.tasks.DealerManager;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RoomServiceImpl implements RoomService {

  private final int MAX_ROOM_SIZE = 10;

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private DealerManager dealerManager;

  @Override
  public RoomDto getRoomById(HttpSession session, String id) {
    // UUID validation
    UUID.fromString(id);

    Optional<Room> maybeRoom = roomRepository.findById(id);
    Room room;
    if (maybeRoom.isPresent()) {
      room = maybeRoom.get();
    } else {
      if (roomRepository.count() >= MAX_ROOM_SIZE) {
        throw new RuntimeException("Room is full");
      }
      room = Room.builder().id(id).status(Status.START).build();
      roomRepository.save(room);
    }

    String nickname = (String) session.getAttribute(SessionKey.NICKNAME);
    if (Objects.isNull(nickname)) {
      List<String> unusedNames = CollectionUtils.isEmpty(room.getMembers()) ? Nickname.NICKNAME_LIST
          : Nickname.NICKNAME_LIST.stream().filter((name) -> {
            return !room.getMembers().contains(name);
          }).collect(Collectors.toList());

      nickname = unusedNames.get((int) (Math.random() * unusedNames.size()));
      session.setAttribute(SessionKey.NICKNAME, nickname);
    }

    return RoomDto.builder()
        .id(id)
        .yourName(nickname)
        .status(room.getStatus()).build();
  }

  @Override
  public void subscribe(String id, DeferredResult<String> deferredResult) {
    dealerManager.getLockMap().putIfAbsent(id, new Object());
    Object lock = dealerManager.getLockMap().get(id);
    synchronized (lock) {
      try {
        lock.wait();
      } catch (InterruptedException e) {
        // do nothing
      }
    }
    log.info("subscribe: " + id + " send queue");
    deferredResult.setResult("queue");
  }
}
