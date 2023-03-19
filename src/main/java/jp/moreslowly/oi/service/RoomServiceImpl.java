package jp.moreslowly.oi.service;

import org.springframework.stereotype.Service;

import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.dto.RoomDto;

@Service
public class RoomServiceImpl implements RoomService {
  @Override
  public RoomDto getRoomById(String id) {
    return RoomDto.builder().id(id).status(Status.START).build();
  }
}
