package com.timetable.backend.service;

import com.timetable.backend.model.Room;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.model.enums.SubjectType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RoomCompatibilityTest {

    private Subject createSubject(String code, String name, SubjectType type) {
        return new Subject(code, name, code, type, null, null, null);
    }

    private Room createRoom(String roomNumber, RoomType type) {
        return new Room(roomNumber, type, 70, "E-Block", null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"E-701", "E-702", "E-703", "E-704"})
    @DisplayName("TRAINING in each of E-701 through E-704: allowed")
    void testTrainingAllowedInE701ThroughE704(String roomNumber) {
        Room room = createRoom(roomNumber, RoomType.LAB);
        Subject training = createSubject("TT2101", "Technical Training", SubjectType.TRAINING);

        assertTrue(RoomCompatibilityValidator.isCompatible(room, training),
                "Technical Training should be allowed in " + roomNumber);
        assertNull(RoomCompatibilityValidator.getIncompatibilityReason(room, training));
    }

    @ParameterizedTest
    @ValueSource(strings = {"E-701", "E-702", "E-703", "E-704"})
    @DisplayName("THEORY in E-701 through E-704: rejected")
    void testTheoryRejectedInE701ThroughE704(String roomNumber) {
        Room room = createRoom(roomNumber, RoomType.LAB);
        Subject theory = createSubject("EMA2110", "Electronic Devices and Circuits", SubjectType.THEORY);

        assertFalse(RoomCompatibilityValidator.isCompatible(room, theory),
                "Theory subject should be rejected in " + roomNumber);
        String reason = RoomCompatibilityValidator.getIncompatibilityReason(room, theory);
        assertNotNull(reason);
        assertTrue(reason.contains("reserved exclusively for TRAINING"),
                "Reason should mention training exclusivity: " + reason);
    }

    @ParameterizedTest
    @ValueSource(strings = {"E-701", "E-702", "E-703", "E-704"})
    @DisplayName("Ordinary LAB subjects in E-701 through E-704: rejected")
    void testOrdinaryLabRejectedInE701ThroughE704(String roomNumber) {
        Room room = createRoom(roomNumber, RoomType.LAB);
        Subject edcLab = createSubject("EMA2110-LAB", "Electronic Devices and Circuits Lab", SubjectType.LAB);
        Subject ssLab = createSubject("EMA2115-LAB", "Signals and Systems Lab", SubjectType.LAB);
        Subject javaLab = createSubject("EMA2X19-LAB", "Java Lab", SubjectType.LAB);

        for (Subject lab : new Subject[]{edcLab, ssLab, javaLab}) {
            assertFalse(RoomCompatibilityValidator.isCompatible(room, lab),
                    lab.getName() + " should be rejected in " + roomNumber);
            String reason = RoomCompatibilityValidator.getIncompatibilityReason(room, lab);
            assertNotNull(reason);
            assertTrue(reason.contains("reserved exclusively for TRAINING"),
                    "Reason should mention training exclusivity: " + reason);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"E-301", "E-609", "E-104"})
    @DisplayName("Ordinary labs in E-301, E-609 and E-104: allowed")
    void testOrdinaryLabsAllowedInE301E609E104(String roomNumber) {
        Room room = createRoom(roomNumber, RoomType.LAB);
        Subject edcLab = createSubject("EMA2110-LAB", "Electronic Devices and Circuits Lab", SubjectType.LAB);
        Subject ssLab = createSubject("EMA2115-LAB", "Signals and Systems Lab", SubjectType.LAB);
        Subject javaLab = createSubject("EMA2X19-LAB", "Java Lab", SubjectType.LAB);

        for (Subject lab : new Subject[]{edcLab, ssLab, javaLab}) {
            assertTrue(RoomCompatibilityValidator.isCompatible(room, lab),
                    lab.getName() + " should be allowed in " + roomNumber);
            assertNull(RoomCompatibilityValidator.getIncompatibilityReason(room, lab));
        }
    }

    @Test
    @DisplayName("THEORY in E-202: rejected")
    void testTheoryRejectedInE202() {
        Room seminarHall = createRoom("E-202", RoomType.SEMINAR_HALL);
        Subject theory = createSubject("EMA2118", "Electromagnetic Theory and Transmission Lines", SubjectType.THEORY);

        assertFalse(RoomCompatibilityValidator.isCompatible(seminarHall, theory),
                "Theory subject should be rejected in SEMINAR_HALL E-202");
        String reason = RoomCompatibilityValidator.getIncompatibilityReason(seminarHall, theory);
        assertNotNull(reason);
        assertTrue(reason.contains("SEMINAR_HALL") && reason.contains("THEORY"),
                "Reason should mention SEMINAR_HALL theory rejection: " + reason);
    }

    @Test
    @DisplayName("Integrated Project-I in E-202: allowed")
    void testIntegratedProjectIAllowedInE202() {
        Room seminarHall = createRoom("E-202", RoomType.SEMINAR_HALL);
        Subject project = createSubject("EVA2101", "Integrated Project-I", SubjectType.ACTIVITY);

        assertTrue(RoomCompatibilityValidator.isCompatible(seminarHall, project),
                "Integrated Project-I should be allowed in SEMINAR_HALL E-202");
        assertNull(RoomCompatibilityValidator.getIncompatibilityReason(seminarHall, project));
    }
}
