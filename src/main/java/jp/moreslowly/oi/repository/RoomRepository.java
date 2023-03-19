package jp.moreslowly.oi.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.moreslowly.oi.dao.Room;

@Repository
public interface RoomRepository extends CrudRepository<Room, String> {
  boolean existsById(String id);
  Optional<Room> findById(String id);
}
