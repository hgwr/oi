package jp.moreslowly.oi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.common.RoomLimitation;
import jp.moreslowly.oi.common.SessionKey;
import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.dto.BetDto;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.exception.BadRequestException;
import jp.moreslowly.oi.models.Nickname;
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

  private Room findOrCreateRoom(String id) {
    Optional<Room> maybeRoom = roomRepository.findById(id);
    Room room;
    if (maybeRoom.isPresent()) {
      room = maybeRoom.get();
    } else {
      if (roomRepository.count() >= RoomLimitation.MAX_ROOM_SIZE) {
        throw new BadRequestException("Room is full");
      }
      room = Room.builder()
          .id(id)
          .status(Status.START)
          .members(new ArrayList<>())
          .wallets(new HashMap<>())
          .build();
      dealerManager.updateAndNotify(id, () -> {
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      });
    }
    return room;
  }

  private String getNickname(HttpSession session, Room room) {
    String nickname = (String) session.getAttribute(SessionKey.NICKNAME);
    if (Objects.isNull(nickname)) {
      List<String> unusedNames = CollectionUtils.isEmpty(room.getMembers()) ? Nickname.NICKNAME_LIST
          : Nickname.NICKNAME_LIST.stream().filter((name) -> {
            return !room.getMembers().contains(name);
          }).collect(Collectors.toList());
      if (unusedNames.isEmpty()) {
        throw new BadRequestException("Nickname is full");
      }
      nickname = unusedNames.get((int) (Math.random() * unusedNames.size()));
      session.setAttribute(SessionKey.NICKNAME, nickname);
      if (CollectionUtils.isEmpty(room.getMembers())) {
        room.setMembers(new ArrayList<>());
      }
      room.getMembers().add(nickname);
      dealerManager.updateAndNotify(room.getId(), () -> {
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      });
    }
    return nickname;
  }

  @Override
  public RoomDto enterRoom(HttpSession session, String id) {
    String enteredRoomId = (String) session.getAttribute(SessionKey.ROOM_ID);
    if (Objects.isNull(enteredRoomId)) {
      session.setAttribute(SessionKey.ROOM_ID, id);
      enteredRoomId = id;
    }
    if (!enteredRoomId.equals(id)) {
      session.removeAttribute(SessionKey.NICKNAME);
      session.setAttribute(SessionKey.ROOM_ID, id);
    }

    Room room = findOrCreateRoom(id);

    String nickname = getNickname(session, room);

    if (!room.getMembers().contains(nickname)) {
      room.getMembers().add(nickname);
      dealerManager.updateAndNotify(id, () -> {
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      });
    }

    dealerManager.updateAndNotify(id, () -> {
      if (Objects.isNull(room.getWallets())) {
        room.setWallets(new HashMap<>());
      }
      room.getWallets().putIfAbsent(nickname, 10000);
      roomRepository.save(room);
      return UpdateStatus.UPDATED;
    });

    return RoomDto.fromEntity(room, nickname);
  }

  private ExecutorService runners = Executors
      .newFixedThreadPool(RoomLimitation.MAX_ROOM_SIZE * RoomLimitation.MAX_MEMBER_SIZE);

  @Override
  public void subscribe(String id, String yourName, DeferredResult<RoomDto> deferredResult) {
    log.info("## subscribed");
    runners.execute(() -> {
      log.info("### runAsync");
      dealerManager.waitForUpdating(id, () -> {
        Room room = roomRepository.findById(id).orElse(null);
        RoomDto dto = Objects.isNull(room) ? null : RoomDto.fromEntity(room, yourName);
        deferredResult.setResult(dto);
      });
    });
  }

  @Override
  public void reset(String id) {
    dealerManager.updateAndNotify(id, () -> {
      Room room = roomRepository.findById(id).orElse(null);
      if (Objects.nonNull(room)) {
        room.setStatus(Status.START);
        room.setDeck(null);
        room.setHands1(null);
        room.setHands2(null);
        room.setHands3(null);
        room.setHands4(null);
        room.setHands5(null);
        room.setHands6(null);
        room.setHands7(null);
        room.setBets(null);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
      }
      return UpdateStatus.UPDATED;
    });
  }

  @Override
  public void bet(BetDto betDto) {
    dealerManager.updateAndNotify(betDto.getRoomId(), () -> {
      Room room = roomRepository.findById(betDto.getRoomId()).orElse(null);
      if (Objects.isNull(room)) {
        return UpdateStatus.NOT_UPDATED;
      }

      if (CollectionUtils.isEmpty(room.getBets())) {
        room.setBets(new ArrayList<>());
      }
      Integer wallet = room.getWallets().get(betDto.getUserName());
      wallet -= betDto.getBetAmount();
      room.getWallets().put(betDto.getUserName(), wallet);
      room.getBets().add(betDto.toEntity());
      roomRepository.save(room);

      return UpdateStatus.UPDATED;
    });
  }
}
