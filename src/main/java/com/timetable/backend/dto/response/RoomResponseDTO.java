package com.timetable.backend.dto.response;

import com.timetable.backend.model.enums.RoomType;

public class RoomResponseDTO {
    private Long id;
    private String roomNumber;
    private RoomType type;
    private int capacity;
    private String block;
    private Long departmentId;
    private String departmentCode;

    public RoomResponseDTO() {
    }

    public RoomResponseDTO(Long id, String roomNumber, RoomType type, int capacity, String block, Long departmentId, String departmentCode) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.block = block;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
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

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
}
