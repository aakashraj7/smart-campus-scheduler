package com.campus.scheduler.controller;

import com.campus.scheduler.dao.FacultyDao;
import com.campus.scheduler.model.Faculty;
import com.campus.scheduler.util.ValidationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculties")
@CrossOrigin(origins = "*")
public class FacultyController {

    private final FacultyDao facultyDao;

    public FacultyController(FacultyDao facultyDao) {
        this.facultyDao = facultyDao;
    }

    @GetMapping
    public List<Faculty> getAllFaculties() {
        return facultyDao.getAllFaculties();
    }

    @GetMapping("/{facultyId}")
    public ResponseEntity<?> getFaculty(@PathVariable String facultyId) {
        Faculty faculty = facultyDao.findByFacultyId(facultyId.trim().toUpperCase());
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculty);
    }

    @PostMapping
    public ResponseEntity<?> saveFaculty(@RequestBody Faculty faculty) {
        if (!ValidationUtil.isValidCode(faculty.getFacultyId())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Faculty ID must be alphanumeric (e.g. FAC001)"));
        }
        if (!ValidationUtil.isValidMinLength(faculty.getName(), 2)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Faculty name is required"));
        }
        if (!ValidationUtil.isNonEmpty(faculty.getDepartment())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Department is required"));
        }
        if (!ValidationUtil.isValidEmail(faculty.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid email address"));
        }
        if (!ValidationUtil.isValidMobile(faculty.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phone number must be a 10-digit number starting with 6-9"));
        }
        if (faculty.getMaxPeriodsPerDay() < 1 || faculty.getMaxPeriodsPerDay() > 8) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Max periods per day must be between 1 and 8"));
        }
        if (faculty.getMaxPeriodsPerWeek() < 1 || faculty.getMaxPeriodsPerWeek() > 40) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Max periods per week must be between 1 and 40"));
        }

        faculty.setFacultyId(faculty.getFacultyId().trim().toUpperCase());
        faculty.setName(faculty.getName().trim());
        faculty.setDepartment(faculty.getDepartment().trim().toUpperCase());
        faculty.setEmail(faculty.getEmail().trim());
        faculty.setPhone(faculty.getPhone().trim());

        facultyDao.saveOrUpdate(faculty);
        return ResponseEntity.ok(Map.of("success", true, "message", "Faculty saved successfully", "faculty", faculty));
    }

    @DeleteMapping("/{facultyId}")
    public ResponseEntity<?> deleteFaculty(@PathVariable String facultyId) {
        boolean deleted = facultyDao.deleteByFacultyId(facultyId.trim().toUpperCase());
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Faculty deleted"));
        } else {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Faculty not found"));
        }
    }
}
