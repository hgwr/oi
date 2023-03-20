package jp.moreslowly.oi.controllers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.common.RoomLimitation;
import jp.moreslowly.oi.common.SessionKey;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.service.RoomService;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/room")
@Log4j2
public class RoomController {

  @Autowired
  private RoomService roomService;

  @Autowired
  private HttpSession session;

  @GetMapping("/")
  public RoomDto enterRoom(@RequestParam String id) {
    // UUID validation
    UUID.fromString(id);

    return roomService.enterRoom(session, id);
  }

  @GetMapping("/subscribe")
  public DeferredResult<RoomDto> subscribe(@RequestParam String id) {
    // UUID validation
    UUID.fromString(id);

    String yourName = session.getAttribute(SessionKey.NICKNAME).toString();

    DeferredResult<RoomDto> deferredResult = new DeferredResult<>();
    CompletableFuture.runAsync(() -> {
      roomService.subscribe(id, yourName, deferredResult);
    });

    return deferredResult;
  }
}
