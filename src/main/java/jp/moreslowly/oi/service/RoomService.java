package jp.moreslowly.oi.service;

import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.dto.RoomDto;

public interface RoomService {

  RoomDto enterRoom(HttpSession session, String id);

  void subscribe(String id, String yourName, DeferredResult<RoomDto> deferredResult);

  void reset(String id);
}
