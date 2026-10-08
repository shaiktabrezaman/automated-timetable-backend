package com.timetable.backend.service;

import com.timetable.backend.dto.request.RoomRequestDTO;
import com.timetable.backend.dto.response.RoomResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.Department;
import com.timetable.backend.model.Room;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.repository.DepartmentRepository;
import com.timetable.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;

    public RoomService(RoomRepository roomRepository, DepartmentRepository departmentRepository) {
        this.roomRepository = roomRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRooms(RoomType type, Long departmentId) {
        List<Room> rooms;
        if (type != null) {
            rooms = roomRepository.findByType(type);
        } else if (departmentId != null) {
            rooms = roomRepository.findByDepartmentIdOrDepartmentIsNull(departmentId);
        } else {
            rooms = roomRepository.findAll();
        }
        return rooms.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long id) {
        Room room = findEntityById(id);
        return mapToDTO(room);
    }

    public RoomResponseDTO createRoom(RoomRequestDTO dto) {
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new ValidationException("Room with number '" + dto.getRoomNumber() + "' already exists");
        }
        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));
        }
        Room room = new Room(dto.getRoomNumber().trim(), dto.getType(), dto.getCapacity(), dto.getBlock(), dept);
        Room saved = roomRepository.save(room);
        return mapToDTO(saved);
    }

    public RoomResponseDTO updateRoom(Long id, RoomRequestDTO dto) {
        Room room = findEntityById(id);
        if (!room.getRoomNumber().equalsIgnoreCase(dto.getRoomNumber()) && roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new ValidationException("Room with number '" + dto.getRoomNumber() + "' already exists");
        }
        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));
        }
        room.setRoomNumber(dto.getRoomNumber().trim());
        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setBlock(dto.getBlock());
        room.setDepartment(dept);
        Room updated = roomRepository.save(room);
        return mapToDTO(updated);
    }

    public void deleteRoom(Long id) {
        Room room = findEntityById(id);
        roomRepository.delete(room);
    }

    public Room findEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
    }

    private RoomResponseDTO mapToDTO(Room r) {
        return new RoomResponseDTO(
                r.getId(),
                r.getRoomNumber(),
                r.getType(),
                r.getCapacity(),
                r.getBlock(),
                r.getDepartment() != null ? r.getDepartment().getId() : null,
                r.getDepartment() != null ? r.getDepartment().getCode() : null
        );
    }
}
