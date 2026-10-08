package com.timetable.backend.dto.request;

import com.timetable.backend.model.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RoomRequestDTO {

    @NotBlank(message = "Room number is required")
    @Size(max = 50, message = "Room number must be at most 50 characters")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomType type = RoomType.CLASSROOM;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity = 70;

    private String block;

    private Long departmentId;

    public RoomRequestDTO() {
    }

    public RoomRequestDTO(String roomNumber, RoomType type, Integer capacity, String block, Long departmentId) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.block = block;
        this.departmentId = departmentId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
