package com.campus.scheduler.controller;

import com.campus.scheduler.dao.*;
import com.campus.scheduler.engine.TimetableGenerator;
import com.campus.scheduler.model.*;
import com.campus.scheduler.util.Exporter;
import com.campus.scheduler.util.TableFormatter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
public class TimetableController {

    private final TimetableDao timetableDao;
    private final AcademicConfigDao configDao;
    private final ClassDao classDao;
    private final FacultyDao facultyDao;
    private final SubjectDao subjectDao;

    public TimetableController(TimetableDao timetableDao, AcademicConfigDao configDao, ClassDao classDao, FacultyDao facultyDao, SubjectDao subjectDao) {
        this.timetableDao = timetableDao;
        this.configDao = configDao;
        this.classDao = classDao;
        this.facultyDao = facultyDao;
        this.subjectDao = subjectDao;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateTimetables() {
        AcademicConfig config = configDao.getConfig();
        List<ClassGroup> classes = classDao.getAllClasses();
        List<Faculty> faculties = facultyDao.getAllFaculties();

        TimetableGenerator generator = new TimetableGenerator(config, classes, faculties);
        SchedulerResult result = generator.generate();

        if (result.isSuccess()) {
            for (Map.Entry<String, List<TimetableSlot>> entry : result.getClassSchedules().entrySet()) {
                String classId = entry.getKey();
                List<TimetableSlot> slots = entry.getValue();
                String ttId = "TT_" + classId + "_" + System.currentTimeMillis();
                Timetable tt = new Timetable(ttId, classId, slots);
                timetableDao.saveOrUpdate(tt);
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Timetables successfully generated for " + result.getClassSchedules().size() + " classes!",
                    "classesScheduled", result.getClassSchedules().keySet()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Unable to generate conflict-free timetable",
                    "errors", result.getErrorMessages()
            ));
        }
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getClassTimetable(@PathVariable String classId) {
        classId = classId.trim().toUpperCase();
        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "No timetable found for Class " + classId));
        }

        AcademicConfig config = configDao.getConfig();
        Map<String, String> shortNameMap = new HashMap<>();
        for (Subject s : subjectDao.getAllSubjects()) {
            shortNameMap.put(s.getSubjectCode(), s.getShortName());
        }

        Map<String, String> facultyNameMap = new HashMap<>();
        for (Faculty f : facultyDao.getAllFaculties()) {
            facultyNameMap.put(f.getFacultyId(), f.getName());
        }

        List<Map<String, Object>> slotList = new ArrayList<>();
        for (TimetableSlot slot : tt.getSlots()) {
            Map<String, Object> map = new HashMap<>();
            map.put("day", slot.getDay());
            map.put("period", slot.getPeriod());
            map.put("classId", slot.getClassId());
            map.put("subjectCode", slot.getSubjectCode());
            map.put("subjectShortName", shortNameMap.getOrDefault(slot.getSubjectCode(), slot.getSubjectCode()));
            map.put("facultyId", slot.getFacultyId());
            map.put("facultyName", facultyNameMap.getOrDefault(slot.getFacultyId(), slot.getFacultyId()));
            slotList.add(map);
        }

        return ResponseEntity.ok(Map.of(
                "classId", tt.getClassId(),
                "timetableId", tt.getTimetableId(),
                "config", config,
                "slots", slotList
        ));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<?> getFacultyTimetable(@PathVariable String facultyId) {
        facultyId = facultyId.trim().toUpperCase();
        Faculty faculty = facultyDao.findByFacultyId(facultyId);
        if (faculty == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Faculty not found: " + facultyId));
        }

        AcademicConfig config = configDao.getConfig();
        List<Timetable> allTimetables = timetableDao.getAllTimetables();

        Map<String, String> shortNameMap = new HashMap<>();
        for (Subject s : subjectDao.getAllSubjects()) {
            shortNameMap.put(s.getSubjectCode(), s.getShortName());
        }

        List<Map<String, Object>> facultySlots = new ArrayList<>();
        for (Timetable tt : allTimetables) {
            for (TimetableSlot slot : tt.getSlots()) {
                if (facultyId.equalsIgnoreCase(slot.getFacultyId())) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("day", slot.getDay());
                    map.put("period", slot.getPeriod());
                    map.put("classId", slot.getClassId());
                    map.put("subjectCode", slot.getSubjectCode());
                    map.put("subjectShortName", shortNameMap.getOrDefault(slot.getSubjectCode(), slot.getSubjectCode()));
                    map.put("facultyId", slot.getFacultyId());
                    map.put("facultyName", faculty.getName());
                    facultySlots.add(map);
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "facultyId", faculty.getFacultyId(),
                "facultyName", faculty.getName(),
                "department", faculty.getDepartment(),
                "maxPeriodsPerDay", faculty.getMaxPeriodsPerDay(),
                "maxPeriodsPerWeek", faculty.getMaxPeriodsPerWeek(),
                "totalAssigned", facultySlots.size(),
                "config", config,
                "slots", facultySlots
        ));
    }

    @PostMapping("/swap")
    public ResponseEntity<?> swapSlots(@RequestBody Map<String, Object> body) {
        String classId = (String) body.get("classId");
        String day1 = (String) body.get("day1");
        int period1 = Integer.parseInt(body.get("period1").toString());
        String day2 = (String) body.get("day2");
        int period2 = Integer.parseInt(body.get("period2").toString());

        if (classId == null || day1 == null || day2 == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "classId, day1, period1, day2, and period2 are required"));
        }

        classId = classId.trim().toUpperCase();
        day1 = day1.trim().toUpperCase();
        day2 = day2.trim().toUpperCase();

        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "No timetable found for Class " + classId));
        }

        TimetableSlot slot1 = findSlot(tt.getSlots(), day1, period1);
        TimetableSlot slot2 = findSlot(tt.getSlots(), day2, period2);

        if (slot1 == null && slot2 == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Both target slots are empty. Nothing to swap."));
        }

        // Perform swap
        String sCode1 = slot1 != null ? slot1.getSubjectCode() : null;
        String fId1 = slot1 != null ? slot1.getFacultyId() : null;

        String sCode2 = slot2 != null ? slot2.getSubjectCode() : null;
        String fId2 = slot2 != null ? slot2.getFacultyId() : null;

        if (slot1 != null) {
            slot1.setSubjectCode(sCode2);
            slot1.setFacultyId(fId2);
        } else {
            tt.getSlots().add(new TimetableSlot(day1, period1, classId, sCode2, fId2));
        }

        if (slot2 != null) {
            slot2.setSubjectCode(sCode1);
            slot2.setFacultyId(fId1);
        } else {
            tt.getSlots().add(new TimetableSlot(day2, period2, classId, sCode1, fId1));
        }

        tt.getSlots().removeIf(s -> s.getSubjectCode() == null);
        timetableDao.saveOrUpdate(tt);

        return ResponseEntity.ok(Map.of("success", true, "message", "Slots successfully swapped!"));
    }

    @GetMapping("/export/csv/{classId}")
    public ResponseEntity<?> exportCsv(@PathVariable String classId) {
        classId = classId.trim().toUpperCase();
        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            return ResponseEntity.status(404).body("Timetable not found for " + classId);
        }

        try {
            AcademicConfig config = configDao.getConfig();
            String path = Exporter.exportClassTimetableToCsv(classId, tt.getSlots(), config);
            File file = new File(path);
            byte[] data = Files.readAllBytes(file.toPath());
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Timetable_" + classId + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error exporting CSV: " + e.getMessage());
        }
    }

    @GetMapping("/export/txt/{classId}")
    public ResponseEntity<?> exportTxt(@PathVariable String classId) {
        classId = classId.trim().toUpperCase();
        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            return ResponseEntity.status(404).body("Timetable not found for " + classId);
        }

        try {
            AcademicConfig config = configDao.getConfig();
            String path = Exporter.exportClassTimetableToTxt(classId, tt.getSlots(), config);
            File file = new File(path);
            byte[] data = Files.readAllBytes(file.toPath());
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Timetable_" + classId + ".txt\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error exporting TXT: " + e.getMessage());
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        List<Faculty> faculties = facultyDao.getAllFaculties();
        List<Timetable> timetables = timetableDao.getAllTimetables();

        Map<String, Integer> assignedMap = new HashMap<>();
        for (Timetable tt : timetables) {
            for (TimetableSlot slot : tt.getSlots()) {
                if (slot.getFacultyId() != null) {
                    assignedMap.put(slot.getFacultyId(), assignedMap.getOrDefault(slot.getFacultyId(), 0) + 1);
                }
            }
        }

        List<Map<String, Object>> reports = new ArrayList<>();
        for (Faculty f : faculties) {
            int assigned = assignedMap.getOrDefault(f.getFacultyId(), 0);
            int max = f.getMaxPeriodsPerWeek();
            double rate = max > 0 ? Math.round(((double) assigned / max) * 1000.0) / 10.0 : 0.0;

            reports.add(Map.of(
                    "facultyId", f.getFacultyId(),
                    "name", f.getName(),
                    "department", f.getDepartment(),
                    "assigned", assigned,
                    "maxWeekly", max,
                    "utilizationRate", rate
            ));
        }

        return ResponseEntity.ok(Map.of(
                "totalFaculties", faculties.size(),
                "totalTimetables", timetables.size(),
                "reports", reports
        ));
    }

    private TimetableSlot findSlot(List<TimetableSlot> slots, String day, int period) {
        for (TimetableSlot s : slots) {
            if (s.getDay().equalsIgnoreCase(day) && s.getPeriod() == period) {
                return s;
            }
        }
        return null;
    }
}
