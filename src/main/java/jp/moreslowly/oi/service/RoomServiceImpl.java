package jp.moreslowly.oi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.moreslowly.oi.dao.Room;
import jp.moreslowly.oi.dao.Room.Status;
import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.repository.RoomRepository;

@Service
public class RoomServiceImpl implements RoomService {

  @Autowired
  private RoomRepository roomRepository;

  @Override
  public RoomDto getRoomById(String id) {
    // UUID validation
    UUID.fromString(id);

    Optional<Room> maybeRoom = roomRepository.findById(id);
    if (maybeRoom.isPresent()) {
      Room room = maybeRoom.get();
      room.setStatus(room.getStatus().next());
      roomRepository.save(room);
      return RoomDto.builder().id(id).status(room.getStatus()).build();
    }

    Room newRoom = Room.builder().id(id).status(Status.START).build();
    roomRepository.save(newRoom);
    return RoomDto.builder().id(id).status(newRoom.getStatus()).build();
  }
}
