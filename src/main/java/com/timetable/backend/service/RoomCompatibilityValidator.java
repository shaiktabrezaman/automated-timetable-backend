package com.timetable.backend.service;

import com.timetable.backend.model.Room;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.model.enums.SubjectType;

import java.util.Set;

public final class RoomCompatibilityValidator {

    public static final Set<String> TRAINING_ROOM_NUMBERS = Set.of("E-701", "E-702", "E-703", "E-704");
    public static final String SEMINAR_HALL_ROOM_NUMBER = "E-202";

    private RoomCompatibilityValidator() {
    }

    public static boolean isTrainingRoom(Room room) {
        if (room == null || room.getRoomNumber() == null) return false;
        return TRAINING_ROOM_NUMBERS.contains(room.getRoomNumber().trim().toUpperCase());
    }

    public static boolean isSeminarHall(Room room) {
        if (room == null) return false;
        return room.getType() == RoomType.SEMINAR_HALL
                || (room.getRoomNumber() != null && room.getRoomNumber().trim().equalsIgnoreCase(SEMINAR_HALL_ROOM_NUMBER));
    }

    public static boolean isCompatible(Room room, Subject subject) {
        if (room == null || subject == null) {
            return true;
        }

        boolean trainingRoom = isTrainingRoom(room);
        boolean seminarHall = isSeminarHall(room);
        SubjectType subjectType = subject.getType();
        RoomType roomType = room.getType();

        // 1. E-701, E-702, E-703, E-704 are exclusive to TRAINING subjects
        if (trainingRoom) {
            return subjectType == SubjectType.TRAINING;
        }

        // 2. TRAINING subjects require a LAB room
        if (subjectType == SubjectType.TRAINING) {
            return roomType == RoomType.LAB;
        }

        // 3. SEMINAR_HALL (E-202) is reserved for project/activity sessions, not ordinary theory or labs
        if (seminarHall) {
            return subjectType == SubjectType.ACTIVITY;
        }

        // 4. Ordinary LAB subjects require a LAB room (and cannot use E-701..704, handled above)
        if (subjectType == SubjectType.LAB) {
            return roomType == RoomType.LAB;
        }

        // 5. THEORY and ELECTIVE subjects require a CLASSROOM (cannot use LAB or SEMINAR_HALL)
        if (subjectType == SubjectType.THEORY || subjectType == SubjectType.ELECTIVE) {
            return roomType == RoomType.CLASSROOM;
        }

        // 6. ACTIVITY subjects (e.g. Integrated Project-I) can use SEMINAR_HALL or CLASSROOM
        if (subjectType == SubjectType.ACTIVITY) {
            return roomType == RoomType.SEMINAR_HALL || roomType == RoomType.CLASSROOM;
        }

        return false;
    }

    public static String getIncompatibilityReason(Room room, Subject subject) {
        if (room == null || subject == null || isCompatible(room, subject)) {
            return null;
        }

        boolean trainingRoom = isTrainingRoom(room);
        boolean seminarHall = isSeminarHall(room);
        SubjectType subjectType = subject.getType();
        RoomType roomType = room.getType();

        if (trainingRoom && subjectType != SubjectType.TRAINING) {
            return "Room '" + room.getRoomNumber() + "' is reserved exclusively for TRAINING subjects and cannot be assigned to " +
                    subjectType + " subject '" + subject.getName() + "'.";
        }

        if (subjectType == SubjectType.TRAINING && roomType != RoomType.LAB) {
            return "Subject '" + subject.getName() + "' is a TRAINING subject and requires a LAB room, but room '" +
                    room.getRoomNumber() + "' is of type " + roomType + ".";
        }

        if (seminarHall && (subjectType == SubjectType.THEORY || subjectType == SubjectType.ELECTIVE)) {
            return "Room '" + room.getRoomNumber() + "' is a SEMINAR_HALL reserved for project/activity sessions and cannot be assigned to THEORY subject '" +
                    subject.getName() + "'.";
        }

        if (seminarHall && (subjectType == SubjectType.LAB || subjectType == SubjectType.TRAINING)) {
            return "Subject '" + subject.getName() + "' requires a LAB room, but room '" +
                    room.getRoomNumber() + "' is of type SEMINAR_HALL.";
        }

        if (subjectType == SubjectType.LAB && roomType != RoomType.LAB) {
            return "Subject '" + subject.getName() + "' is a LAB subject and requires a LAB room, but room '" +
                    room.getRoomNumber() + "' is of type " + roomType + ".";
        }

        if ((subjectType == SubjectType.THEORY || subjectType == SubjectType.ELECTIVE) && roomType == RoomType.LAB) {
            return "Subject '" + subject.getName() + "' is not a LAB subject and cannot be assigned to LAB room '" +
                    room.getRoomNumber() + "'.";
        }

        if ((subjectType == SubjectType.THEORY || subjectType == SubjectType.ELECTIVE) && roomType != RoomType.CLASSROOM) {
            return "Subject '" + subject.getName() + "' is a THEORY subject and requires a CLASSROOM, but room '" +
                    room.getRoomNumber() + "' is of type " + roomType + ".";
        }

        if (subjectType == SubjectType.ACTIVITY && roomType == RoomType.LAB) {
            return "Subject '" + subject.getName() + "' cannot be assigned to LAB room '" +
                    room.getRoomNumber() + "'.";
        }

        return "Subject '" + subject.getName() + "' (" + subjectType + ") cannot be assigned to room '" +
                room.getRoomNumber() + "' (" + roomType + ").";
    }
}
