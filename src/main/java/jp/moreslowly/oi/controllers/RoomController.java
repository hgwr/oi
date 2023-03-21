package jp.moreslowly.oi.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.common.SessionKey;
import jp.moreslowly.oi.dto.BetDto;
import jp.moreslowly.oi.dto.IdDto;
import jp.moreslowly.oi.dto.RequestCardDto;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.exception.UnprocessableContentException;
import jp.moreslowly.oi.service.RoomService;

@RestController
@RequestMapping("/api/room")
public class RoomController {

  @Autowired
  private RoomService roomService;

  @Autowired
  private HttpSession session;

  @GetMapping("/")
  public List<String> getRoomIdList() {
    return roomService.getRoomIdList();
  }

  @PostMapping("/")
  public RoomDto enterRoom(@RequestBody IdDto dto) {
    // UUID validation
    UUID.fromString(dto.getId());

    return roomService.enterRoom(session, dto.getId());
  }

  @PostMapping("/subscribe")
  public DeferredResult<RoomDto> subscribe(@RequestBody IdDto dto) {
    // UUID validation
    UUID.fromString(dto.getId());

    DeferredResult<RoomDto> deferredResult = new DeferredResult<>();
    roomService.subscribe(dto.getId(), session, deferredResult);

    return deferredResult;
  }

  @PostMapping("/reset")
  public void reset(@RequestBody IdDto dto) {
    // UUID validation
    UUID.fromString(dto.getId());

    roomService.reset(dto.getId());
  }

  @PostMapping("/bet")
  public void bet(@RequestBody BetDto dto) {
    // UUID validation
    UUID roomIdUUID = UUID.fromString(dto.getRoomId());

    String sessionRoomId = (String) session.getAttribute(SessionKey.ROOM_ID);
    UUID sessionRoomIdUUID = UUID.fromString(sessionRoomId);
    if (!roomIdUUID.equals(sessionRoomIdUUID)) {
      throw new UnprocessableContentException("Invalid room id");
    }

    String sessionNickname = (String) session.getAttribute(SessionKey.NICKNAME);
    if (!dto.getUserName().equals(sessionNickname)) {
      throw new UnprocessableContentException("Invalid nickname on bet validation");
    }

    roomService.bet(session, dto);
  }

  @PostMapping("/requestCard")
  public void requestOneMore(@RequestBody RequestCardDto dto) {
    // UUID validation
    UUID roomIdUUID = UUID.fromString(dto.getRoomId());

    String sessionRoomId = (String) session.getAttribute(SessionKey.ROOM_ID);
    UUID sessionRoomIdUUID = UUID.fromString(sessionRoomId);
    if (!roomIdUUID.equals(sessionRoomIdUUID)) {
      throw new UnprocessableContentException("Invalid room id");
    }

    String sessionNickname = (String) session.getAttribute(SessionKey.NICKNAME);
    if (!dto.getUserName().equals(sessionNickname)) {
      throw new UnprocessableContentException("Invalid nickname on request card validation");
    }

    roomService.requestCard(session, dto);
  }
}
