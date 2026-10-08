package com.timetable.backend.repository;

import com.timetable.backend.model.Room;
import com.timetable.backend.model.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);
    List<Room> findByType(RoomType type);
    List<Room> findByDepartmentId(Long departmentId);
    List<Room> findByDepartmentIdOrDepartmentIsNull(Long departmentId);
}
