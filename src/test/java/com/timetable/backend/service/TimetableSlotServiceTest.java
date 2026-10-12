package com.timetable.backend.service;

import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.*;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.repository.FacultyRepository;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.SubjectRepository;
import com.timetable.backend.repository.TimetableSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimetableSlotServiceTest {

    @Mock
    private TimetableSlotRepository slotRepository;

    @Mock
    private SectionService sectionService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CourseOfferingService courseOfferingService;

    @Mock
    private SubjectRepository subjectRepository;

    private TimetableSlotService timetableSlotService;

    private Section sectionA;
    private Department department;
    private Semester semester;
    private AcademicYear academicYear;
    private List<TimetableSlot> initialAcademicSlots;

    @BeforeEach
    void setUp() {
        timetableSlotService = new TimetableSlotService(
                slotRepository,
                sectionService,
                subjectService,
                facultyRepository,
                roomRepository,
                courseOfferingService,
                subjectRepository
        );

        department = new Department();
        department.setId(1L);
        department.setCode("ECE");
        department.setName("Electronics & Communication Engineering");

        academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setYearName("2026-27");

        semester = new Semester();
        semester.setId(2L);
        semester.setCode("II-I");
        semester.setName("Semester II-I");

        sectionA = new Section();
        sectionA.setId(3L);
        sectionA.setCode("ECE-A");
        sectionA.setName("II Year ECE Section A");
        sectionA.setStudentCount(66);
        sectionA.setDepartment(department);
        sectionA.setSemester(semester);
        sectionA.setAcademicYear(academicYear);

        // Build 30 academic slots (5 slots per day, 2 empty periods per day across Mon-Sat)
        initialAcademicSlots = new ArrayList<>();
        Subject math = new Subject("MATH", "Mathematics", "MATH", SubjectType.THEORY, department, semester, null);
        math.setId(101L);

        Faculty faculty = new Faculty();
        faculty.setId(201L);
        faculty.setName("Prof. Smith");

        Room room = new Room();
        room.setId(301L);
        room.setRoomNumber("E-314");

        long slotIdGen = 1000L;
        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        };

        // On each day, assign periods 1, 2, 3, 5, 6 (leaving period 4 and 7 empty -> 2 empty per day * 6 = 12)
        for (DayOfWeek day : days) {
            for (int p : new int[]{1, 2, 3, 5, 6}) {
                TimetableSlot slot = new TimetableSlot();
                slot.setId(slotIdGen++);
                slot.setSection(sectionA);
                slot.setDay(day);
                slot.setPeriodIndex(p);
                slot.setSpanPeriods(1);
                slot.setMultiPeriodHead(false);
                slot.setMultiPeriodContinuation(false);
                slot.setSubject(math);
                slot.setFaculty(faculty);
                slot.setRoom(room);
                initialAcademicSlots.add(slot);
            }
        }
        assertEquals(30, initialAcademicSlots.size());
    }

    @Test
    @DisplayName("fillActivities creates 12 activity slots with exact frequencies: LIB 3, NPTEL 3, SPORTS 2, COUNS 2, NSS 2")
    void testFillActivities_Success() {
        when(sectionService.findEntityById(3L)).thenReturn(sectionA);

        // When queried first time, returns the 30 academic slots
        List<TimetableSlot> slotsInDb = new ArrayList<>(initialAcademicSlots);
        when(slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(3L))
                .thenAnswer(invocation -> new ArrayList<>(slotsInDb));

        // Mock subject lookup and creation
        Map<String, Subject> createdSubjects = new HashMap<>();
        when(subjectRepository.findByCode(any())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(createdSubjects.get(code));
        });
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> {
            Subject s = invocation.getArgument(0);
            s.setId((long) (createdSubjects.size() + 500));
            createdSubjects.put(s.getCode(), s);
            return s;
        });

        // Mock slot saveAll to add to slotsInDb
        when(slotRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<TimetableSlot> toSave = invocation.getArgument(0);
            long idGen = 2000L;
            for (TimetableSlot s : toSave) {
                s.setId(idGen++);
                slotsInDb.add(s);
            }
            return toSave;
        });

        List<TimetableSlotResponseDTO> result = timetableSlotService.fillActivities(3L);

        // Assert 42 slots returned
        assertEquals(42, result.size());

        // Verify saveAll was called with exactly 12 activity slots
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TimetableSlot>> captor = ArgumentCaptor.forClass(List.class);
        verify(slotRepository, times(1)).saveAll(captor.capture());
        List<TimetableSlot> savedActivities = captor.getValue();
        assertEquals(12, savedActivities.size());

        // Verify weekly activity frequencies: LIB: 3, NPTEL: 3, SPORTS: 2, COUNS: 2, NSS/NCC: 2
        Map<String, Long> freq = savedActivities.stream()
                .collect(Collectors.groupingBy(s -> s.getSubject().getCode(), Collectors.counting()));
        assertEquals(3L, freq.get("ACT-LIB"));
        assertEquals(3L, freq.get("ACT-NPT"));
        assertEquals(2L, freq.get("ACT-SPT"));
        assertEquals(2L, freq.get("ACT-CNS"));
        assertEquals(2L, freq.get("ACT-NSS"));

        // Verify null faculty and room on every activity slot
        for (TimetableSlot slot : savedActivities) {
            assertNull(slot.getFaculty(), "Faculty must be null for activity slots");
            assertNull(slot.getRoom(), "Room must be null for activity slots");
            assertEquals(1, slot.getSpanPeriods());
            assertFalse(slot.isMultiPeriodHead());
            assertFalse(slot.isMultiPeriodContinuation());
            assertEquals(SubjectType.ACTIVITY, slot.getSubject().getType());
            assertTrue(slot.getPeriodIndex() >= 1 && slot.getPeriodIndex() <= 7);
        }

        // Verify coordinate uniqueness
        Set<String> coords = new HashSet<>();
        for (TimetableSlot slot : slotsInDb) {
            String key = slot.getDay() + ":" + slot.getPeriodIndex();
            assertTrue(coords.add(key), "Duplicate coordinate found: " + key);
        }
        assertEquals(42, coords.size());

        // Verify all 30 original academic slots remain unchanged
        for (TimetableSlot original : initialAcademicSlots) {
            TimetableSlot found = slotsInDb.stream()
                    .filter(s -> Objects.equals(s.getId(), original.getId()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(found);
            assertEquals("MATH", found.getSubject().getCode());
            assertEquals("Prof. Smith", found.getFaculty().getName());
            assertEquals("E-314", found.getRoom().getRoomNumber());
        }
    }

    @Test
    @DisplayName("fillActivities is idempotent: calling second time returns 42 slots without creating duplicates")
    void testFillActivities_Idempotent() {
        when(sectionService.findEntityById(3L)).thenReturn(sectionA);

        // Prepare 42 slots (30 academic + 12 activity)
        List<TimetableSlot> alreadyFilledSlots = new ArrayList<>(initialAcademicSlots);
        Subject lib = new Subject("ACT-LIB", "Library", "LIB", SubjectType.ACTIVITY, department, semester, null);
        for (int i = 0; i < 12; i++) {
            TimetableSlot actSlot = new TimetableSlot();
            actSlot.setId(5000L + i);
            actSlot.setSection(sectionA);
            actSlot.setDay(DayOfWeek.MONDAY);
            actSlot.setPeriodIndex(4);
            actSlot.setSubject(lib);
            alreadyFilledSlots.add(actSlot);
        }
        assertEquals(42, alreadyFilledSlots.size());

        when(slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(3L)).thenReturn(alreadyFilledSlots);

        List<TimetableSlotResponseDTO> result = timetableSlotService.fillActivities(3L);

        assertEquals(42, result.size());
        verify(slotRepository, never()).saveAll(any());
        verify(subjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("fillActivities reuses existing activity subjects without duplicate subject creation")
    void testFillActivities_ReuseExistingSubjects() {
        when(sectionService.findEntityById(3L)).thenReturn(sectionA);

        List<TimetableSlot> slotsInDb = new ArrayList<>(initialAcademicSlots);
        when(slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(3L))
                .thenAnswer(invocation -> new ArrayList<>(slotsInDb));

        // Existing subject records in repo
        Subject existingLib = new Subject("ACT-LIB", "Library", "LIB", SubjectType.ACTIVITY, department, semester, null);
        existingLib.setId(901L);
        when(subjectRepository.findByCode("ACT-LIB")).thenReturn(Optional.of(existingLib));
        when(subjectRepository.findByCode("ACT-NPT")).thenReturn(Optional.of(new Subject("ACT-NPT", "NPTEL Self-Study", "NPTEL", SubjectType.ACTIVITY, department, semester, null)));
        when(subjectRepository.findByCode("ACT-SPT")).thenReturn(Optional.of(new Subject("ACT-SPT", "Sports & Games", "SPORTS", SubjectType.ACTIVITY, department, semester, null)));
        when(subjectRepository.findByCode("ACT-CNS")).thenReturn(Optional.of(new Subject("ACT-CNS", "Counselling / Mentoring", "COUNS", SubjectType.ACTIVITY, department, semester, null)));
        when(subjectRepository.findByCode("ACT-NSS")).thenReturn(Optional.of(new Subject("ACT-NSS", "NSS / NCC / Social Activity", "NSS/NCC", SubjectType.ACTIVITY, department, semester, null)));

        when(slotRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<TimetableSlot> toSave = invocation.getArgument(0);
            long idGen = 3000L;
            for (TimetableSlot s : toSave) {
                s.setId(idGen++);
                slotsInDb.add(s);
            }
            return toSave;
        });

        timetableSlotService.fillActivities(3L);

        // Subject save should never be called since all 5 existed
        verify(subjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("fillActivities fails safely when section has unexpected empty period count")
    void testFillActivities_InvalidSlotCount_ThrowsValidationException() {
        when(sectionService.findEntityById(3L)).thenReturn(sectionA);

        // Only 10 slots present in section (32 empty slots instead of 12)
        List<TimetableSlot> partialSlots = initialAcademicSlots.subList(0, 10);
        when(slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(3L)).thenReturn(partialSlots);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                timetableSlotService.fillActivities(3L)
        );
        assertTrue(ex.getMessage().contains("expected 12 empty periods"));
        verify(slotRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("fillActivities propagates ResourceNotFoundException for invalid section ID")
    void testFillActivities_InvalidSection_ThrowsResourceNotFoundException() {
        when(sectionService.findEntityById(999L)).thenThrow(new ResourceNotFoundException("Section not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                timetableSlotService.fillActivities(999L)
        );
        verify(slotRepository, never()).saveAll(any());
    }
}
