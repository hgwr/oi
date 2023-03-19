package jp.moreslowly.oi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.moreslowly.oi.dto.RoomDto;
import jp.moreslowly.oi.service.RoomService;

@RestController
@RequestMapping("/api/room")
public class RoomController {

  @Autowired
  private RoomService roomService;

  @GetMapping("/")
  public RoomDto getRoom(@RequestParam String id) {
    return roomService.getRoomById(id);
  }
}
