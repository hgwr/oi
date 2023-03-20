package jp.moreslowly.oi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.common.Nickname;
import jp.moreslowly.oi.common.RoomLimitation;
import jp.moreslowly.oi.common.SessionKey;
import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.repository.RoomRepository;
import jp.moreslowly.oi.tasks.DealerManager;
import jp.moreslowly.oi.tasks.DealerManager.UpdateStatus;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RoomServiceImpl implements RoomService {

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private DealerManager dealerManager;

  @Override
  public RoomDto enterRoom(HttpSession session, String id) {
    Optional<Room> maybeRoom = roomRepository.findById(id);
    Room room;
    if (maybeRoom.isPresent()) {
      room = maybeRoom.get();
    } else {
      if (roomRepository.count() >= RoomLimitation.MAX_ROOM_SIZE) {
        throw new RuntimeException("Room is full");
      }
      room = Room.builder()
          .id(id)
          .status(Status.START)
          .members(new ArrayList<>())
          .build();
      dealerManager.updateAndNotify(id, () -> {
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      });
    }

    String nickname = (String) session.getAttribute(SessionKey.NICKNAME);
    if (Objects.isNull(nickname)) {
      List<String> unusedNames = CollectionUtils.isEmpty(room.getMembers()) ? Nickname.NICKNAME_LIST
          : Nickname.NICKNAME_LIST.stream().filter((name) -> {
            return !room.getMembers().contains(name);
          }).collect(Collectors.toList());
      if (unusedNames.isEmpty()) {
        throw new RuntimeException("Nickname is full");
      }
      nickname = unusedNames.get((int) (Math.random() * unusedNames.size()));
      session.setAttribute(SessionKey.NICKNAME, nickname);
      if (CollectionUtils.isEmpty(room.getMembers())) {
        room.setMembers(new ArrayList<>());
      }
      room.getMembers().add(nickname);
      dealerManager.updateAndNotify(id, () -> {
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      });
    }

    if (!room.getMembers().contains(nickname)) {
      room.getMembers().add(nickname);
      dealerManager.updateAndNotify(id, () -> {
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      });
    }

    return RoomDto.fromEntity(room, nickname);
  }

  @Override
  public void subscribe(String id, String yourName, DeferredResult<RoomDto> deferredResult) {
    dealerManager.waitForUpdating(id, () -> {
      Room room = roomRepository.findById(id).orElse(null);
      RoomDto dto = Objects.isNull(room) ? null : RoomDto.fromEntity(room, yourName);
      deferredResult.setResult(dto);
    });
  }
}