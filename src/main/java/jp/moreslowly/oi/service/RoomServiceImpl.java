package jp.moreslowly.oi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

  @Override
  public List<String> getRoomIdList() {
    List<String> roomIdList = new ArrayList<>();
    roomRepository.findAll().forEach(room -> roomIdList.add(room.getId()));
    return roomIdList;
  }

  private Room findOrCreateRoom(String id) {
    dealerManager.updateAndNotify(id, () -> {
      Optional<Room> maybeRoom = roomRepository.findById(id);
      if (maybeRoom.isPresent()) {
        return UpdateStatus.NOT_UPDATED;
      }
      if (roomRepository.count() >= RoomLimitation.MAX_ROOM_SIZE) {
        throw new NoRoomException("No Room");
      }
      Room room = Room.builder()
          .id(id)
          .status(Status.START)
          .members(new ArrayList<>())
          .wallets(new HashMap<>())
          .lastAccessedAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();
      room.getWallets().put("dummy", 0);
      log.info("#### create room {}", id);
      roomRepository.save(room);

      return UpdateStatus.NOT_UPDATED;
    });

    Room room = roomRepository.findById(id)
        .orElseThrow(() -> new UnprocessableContentException(UnprocessableContentException.ROOM_IS_NOT_FOUND));
    room.setLastAccessedAt(LocalDateTime.now());
    dealerManager.updateAndNotify(id, () -> {
      roomRepository.save(room);
      return UpdateStatus.NOT_UPDATED;
    });
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

  private String getNickname(HttpSession session, UUID userId, Room room) {
    dealerManager.updateAndNotify(room.getId(), () -> {
      if (Objects.isNull(room.getMembers())) {
        room.setMembers(new ArrayList<>());
      }

      Optional<Member> maybeMember = room.getMembers().stream().filter(m -> m.getId().equals(userId)).findFirst();
      if (maybeMember.isPresent()) {
        session.setAttribute(SessionKey.NICKNAME, maybeMember.get().getNickname());
        return UpdateStatus.NOT_UPDATED;
      }

      List<String> unusedNames = Nickname.NICKNAME_LIST.stream().filter(name -> {
        return room.getMembers().stream().noneMatch(m -> m.getNickname().equals(name));
      }).collect(Collectors.toList());
      if (unusedNames.isEmpty()) {
        throw new FullMemberException("Nickname is full");
      }
      String nickname = unusedNames.get((int) (Math.random() * unusedNames.size()));
      session.setAttribute(SessionKey.NICKNAME, nickname);
      if (room.getMembers().size() >= RoomLimitation.MAX_MEMBER_SIZE) {
        return UpdateStatus.NOT_UPDATED;
      }
      room.getMembers().add(Member.builder().id(userId).nickname(nickname).build());

      Integer amount = room.getWallets().get(nickname);
      if (Objects.nonNull(amount)) {
        return UpdateStatus.NOT_UPDATED;
      }
      room.getWallets().putIfAbsent(nickname, 10000);

      roomRepository.save(room);
      return UpdateStatus.UPDATED;
    });
    return (String) session.getAttribute(SessionKey.NICKNAME);
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
    UUID userId = getUserId(session);
    String nickname = getNickname(session, userId, room);

    return RoomDto.fromEntity(room, nickname);
  }

  private ExecutorService runners = Executors
      .newFixedThreadPool(RoomLimitation.MAX_ROOM_SIZE * RoomLimitation.MAX_MEMBER_SIZE);

  @Override
  public void subscribe(String id, HttpSession session, DeferredResult<RoomDto> deferredResult) {
    Room room = findOrCreateRoom(id);
    UUID userId = getUserId(session);
    String yourName = getNickname(session, userId, room);

    dealerManager.updateAndNotify(room.getId(), () -> {
      room.setLastAccessedAt(LocalDateTime.now());
      room.getMembers().stream().filter(m -> m.getId().equals(userId)).findFirst().ifPresent(m -> {
        m.setLastAccessedAt(LocalDateTime.now());
      });
      roomRepository.save(room);
      return UpdateStatus.NOT_UPDATED;
    });

    runners.execute(() -> {
      dealerManager.waitForUpdating(id, () -> {
        Room newRoom = roomRepository.findById(id)
            .orElseThrow(() -> new UnprocessableContentException(UnprocessableContentException.ROOM_IS_NOT_FOUND));
        RoomDto dto = RoomDto.fromEntity(newRoom, yourName);
        deferredResult.setResult(dto);
      });
    });
  }

  @Override
  public void reset(String id) {
    dealerManager.updateAndNotify(id, () -> {
      Room room = roomRepository.findById(id)
          .orElseThrow(() -> new UnprocessableContentException(UnprocessableContentException.ROOM_IS_NOT_FOUND));
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
      if (!room.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
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
      if (!room.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
        throw new UnprocessableContentException("You are not member of this room");
      }

      Optional<Bet> maybeBet = room.getBets().stream().filter(
          bet -> Objects.equals(bet.getHandIndex(), requestOneMoreDto.getHandIndex())).findFirst();
      if (!maybeBet.isPresent()) {
        throw new UnprocessableContentException("You are not owner of this hand");
      }
      Bet bet = maybeBet.get();
      if (!bet.getUserName().equals(requestOneMoreDto.getUserName())) {
        throw new UnprocessableContentException("You are not owner of this hand: " + bet.getUserName() + ", " +
            requestOneMoreDto.getUserName());
      }

      List<Card> hands = room.getHandsAt(requestOneMoreDto.getHandIndex());
      if (hands.size() == 3) {
        throw new UnprocessableContentException("You can't request more card");
      }
      Card aCard = room.getDeck().remove(0);
      hands.add(aCard);

      roomRepository.save(room);

      return UpdateStatus.UPDATED;
    });
  }
}
