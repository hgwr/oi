package jp.moreslowly.oi.service;

import java.util.List;

import org.springframework.web.context.request.async.DeferredResult;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.dto.BetDto;
import jp.moreslowly.oi.dto.RequestCardDto;
import jp.moreslowly.oi.dto.RoomDto;

public interface RoomService {

  List<String> getRoomIdList();

  RoomDto enterRoom(HttpSession session, String id);

  void subscribe(String id, HttpSession session, DeferredResult<RoomDto> deferredResult);

  void reset(String id);

  void bet(HttpSession session, BetDto betDto);

  void requestCard(HttpSession session, RequestCardDto requestOneMoreDto);
}
