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
import jp.moreslowly.oi.dao.Bet;
import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.dto.BetDto;
import jp.moreslowly.oi.dto.RequestCardDto;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.exception.FullMemberException;
import jp.moreslowly.oi.exception.NoRoomException;
import jp.moreslowly.oi.exception.UnprocessableContentException;
import jp.moreslowly.oi.models.Card;
import jp.moreslowly.oi.models.Member;
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
        throw new NoRoomException("No Room");
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

  private UUID getUserId(HttpSession session) {
    String userId = (String) session.getAttribute(SessionKey.USER_ID);
    if (Objects.isNull(userId)) {
      userId = UUID.randomUUID().toString();
      session.setAttribute(SessionKey.USER_ID, userId);
    }
    return UUID.fromString(userId);
  }

  private String getNickname(HttpSession session, Room room) {
    dealerManager.updateAndNotify(room.getId(), () -> {
      if (Objects.isNull(room.getMembers())) {
        room.setMembers(new ArrayList<>());
      }
      String nickname = (String) session.getAttribute(SessionKey.NICKNAME);
      UUID userId = getUserId(session);
      if (Objects.isNull(nickname)) {
        List<String> unusedNames = Nickname.NICKNAME_LIST.stream().filter((name) -> {
          return !room.getMembers().stream().anyMatch((m) -> m.getNickname().equals(name));
        }).collect(Collectors.toList());
        if (unusedNames.isEmpty()) {
          throw new FullMemberException("Nickname is full");
        }
        nickname = unusedNames.get((int) (Math.random() * unusedNames.size()));
        session.setAttribute(SessionKey.NICKNAME, nickname);
        if (room.getMembers().size() >= RoomLimitation.MAX_MEMBER_SIZE) {
          return UpdateStatus.NOT_UPDATED;
        }
        room.getMembers().add(Member.builder().id(userId).nickname(nickname).build());
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      }
      return UpdateStatus.NOT_UPDATED;
    });
    return (String) session.getAttribute(SessionKey.NICKNAME);
  }

  private void joinRoom(Room room, UUID userId, String nickname) {
    dealerManager.updateAndNotify(room.getId(), () -> {
      if (Objects.isNull(room.getMembers())) {
        room.setMembers(new ArrayList<>());
      }
      Member member = Member.builder().id(userId).nickname(nickname).build();
      if (!room.getMembers().contains(member)) {
        if (room.getMembers().size() >= RoomLimitation.MAX_MEMBER_SIZE) {
          return UpdateStatus.NOT_UPDATED;
        }
        room.getMembers().add(member);
        roomRepository.save(room);
        return UpdateStatus.UPDATED;
      }
      return UpdateStatus.NOT_UPDATED;
    });
  }

  private void prepareWallets(Room room, String nickname) {
    dealerManager.updateAndNotify(room.getId(), () -> {
      if (Objects.isNull(room.getWallets())) {
        log.warn("##### Clear Wallets !!!! ######");
        room.setWallets(new HashMap<>());
      }
      room.getWallets().putIfAbsent(nickname, 10000);
      roomRepository.save(room);
      return UpdateStatus.UPDATED;
    });
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
    UUID userId = getUserId(session);
    joinRoom(room, userId, nickname);
    prepareWallets(room, nickname);

    return RoomDto.fromEntity(room, nickname);
  }

  private ExecutorService runners = Executors
      .newFixedThreadPool(RoomLimitation.MAX_ROOM_SIZE * RoomLimitation.MAX_MEMBER_SIZE);

  @Override
  public void subscribe(String id, HttpSession session, DeferredResult<RoomDto> deferredResult) {
    UUID userId = getUserId(session);
    Room room = findOrCreateRoom(id);
    String yourName = getNickname(session, room);
    Member you = Member.builder().id(userId).nickname(yourName).build();
    if (CollectionUtils.isEmpty(room.getMembers()) || !room.getMembers().contains(you)) {
      joinRoom(room, userId, yourName);
    }

    runners.execute(() -> {
      dealerManager.waitForUpdating(id, () -> {
        Room newRoom = roomRepository.findById(id)
            .orElseThrow(() -> new UnprocessableContentException("Room is not found"));
        RoomDto dto = RoomDto.fromEntity(newRoom, yourName);
        deferredResult.setResult(dto);
      });
    });
  }

  @Override
  public void reset(String id) {
    dealerManager.updateAndNotify(id, () -> {
      Room room = roomRepository.findById(id).orElseThrow(() -> new UnprocessableContentException("Room is not found"));
      room.reset();
      room.setMembers(new ArrayList<>());
      room.setUpdatedAt(LocalDateTime.now());
      roomRepository.save(room);
      return UpdateStatus.UPDATED;
    });
  }

  @Override
  public void bet(HttpSession session, BetDto betDto) {
    UUID userId = getUserId(session);
    dealerManager.updateAndNotify(betDto.getRoomId(), () -> {
      Optional<Room> maybeRoom = roomRepository.findById(betDto.getRoomId());
      if (!maybeRoom.isPresent()) {
        return UpdateStatus.NOT_UPDATED;
      }
      Room room = maybeRoom.get();
      Member you = Member.builder().id(userId).nickname(betDto.getUserName()).build();
      if (!room.getMembers().contains(you)) {
        throw new UnprocessableContentException("Invalid nickname");
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

  @Override
  public void requestCard(HttpSession session, RequestCardDto requestOneMoreDto) {
    UUID userId = getUserId(session);
    dealerManager.updateAndNotify(requestOneMoreDto.getRoomId(), () -> {
      Optional<Room> maybeRoom = roomRepository.findById(requestOneMoreDto.getRoomId());
      if (!maybeRoom.isPresent()) {
        return UpdateStatus.NOT_UPDATED;
      }
      Room room = maybeRoom.get();
      Member you = Member.builder().id(userId).nickname(requestOneMoreDto.getUserName()).build();
      if (!room.getMembers().contains(you)) {
        throw new UnprocessableContentException("You are not member of this room");
      }

      Optional<Bet> maybeBet = room.getBets().stream().filter(
          bet -> bet.getHandIndex() == requestOneMoreDto.getHandIndex()).findFirst();
      if (!maybeBet.isPresent()) {
        throw new UnprocessableContentException("You are not owner of this hand");
      }
      Bet bet = maybeBet.get();
      if (!bet.getUserName().equals(requestOneMoreDto.getUserName())) {
        throw new UnprocessableContentException("You are not owner of this hand: " + bet.getUserName() + ", " +
            requestOneMoreDto.getUserName());
      }

      Card aCard = room.getDeck().remove(0);
      if (requestOneMoreDto.getHandIndex() == 1) {
        if (room.getHands1().size() == 3) {
          throw new UnprocessableContentException("You can't request more card");
        }
        room.getHands1().add(aCard);
      } else if (requestOneMoreDto.getHandIndex() == 2) {
        if (room.getHands2().size() == 3) {
          throw new UnprocessableContentException("You can't request more card");
        }
        room.getHands2().add(aCard);
      } else if (requestOneMoreDto.getHandIndex() == 3) {
        if (room.getHands3().size() == 3) {
          throw new UnprocessableContentException("You can't request more card");
        }
        room.getHands3().add(aCard);
      } else if (requestOneMoreDto.getHandIndex() == 4) {
        if (room.getHands4().size() == 3) {
          throw new UnprocessableContentException("You can't request more card");
        }
        room.getHands4().add(aCard);
      } else if (requestOneMoreDto.getHandIndex() == 5) {
        if (room.getHands5().size() == 3) {
          throw new UnprocessableContentException("You can't request more card");
        }
        room.getHands5().add(aCard);
      } else if (requestOneMoreDto.getHandIndex() == 6) {
        if (room.getHands6().size() == 3) {
          throw new UnprocessableContentException("You can't request more card");
        }
        room.getHands6().add(aCard);
      }

      room.setUpdatedAt(LocalDateTime.now());
      roomRepository.save(room);

      return UpdateStatus.UPDATED;
    });
  }
}
