package jp.moreslowly.oi.service;

import jakarta.servlet.http.HttpSession;
import jp.moreslowly.oi.dto.RoomDto;

public interface RoomService {
  RoomDto getRoomById(HttpSession session, String id);
}
