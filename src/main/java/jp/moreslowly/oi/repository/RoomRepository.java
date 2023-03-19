package jp.moreslowly.oi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.moreslowly.oi.dao.Room;

@Repository
public interface RoomRepository extends CrudRepository<Room, String> {
}
