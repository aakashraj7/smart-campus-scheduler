package com.campus.scheduler.controller;

import com.campus.scheduler.dao.ClassDao;
import com.campus.scheduler.model.ClassGroup;
import com.campus.scheduler.model.SubjectDemand;
import com.campus.scheduler.util.ValidationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
public class ClassController {

    private final ClassDao classDao;

    public ClassController(ClassDao classDao) {
        this.classDao = classDao;
    }

    @GetMapping
    public List<ClassGroup> getAllClasses() {
        return classDao.getAllClasses();
    }

    @GetMapping("/{classId}")
    public ResponseEntity<?> getClassGroup(@PathVariable String classId) {
        ClassGroup cg = classDao.findByClassId(classId.trim().toUpperCase());
        if (cg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cg);
    }

    @PostMapping
    public ResponseEntity<?> saveClassGroup(@RequestBody ClassGroup classGroup) {
        if (!ValidationUtil.isValidCode(classGroup.getClassId())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Class ID is required (e.g. IT-C)"));
        }
        if (!ValidationUtil.isNonEmpty(classGroup.getDepartment())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Department is required"));
        }
        if (classGroup.getSemester() < 1 || classGroup.getSemester() > 10) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Semester must be between 1 and 10"));
        }

        classGroup.setClassId(classGroup.getClassId().trim().toUpperCase());
        classGroup.setDepartment(classGroup.getDepartment().trim().toUpperCase());

        if (classGroup.getSubjectDemands() != null) {
            for (SubjectDemand demand : classGroup.getSubjectDemands()) {
                if (demand.getPeriodsPerWeek() < 1 || demand.getPeriodsPerWeek() > 25) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Periods per week for " + demand.getSubjectCode() + " must be between 1 and 25"));
                }
                demand.setSubjectCode(demand.getSubjectCode().trim().toUpperCase());
                demand.setFacultyId(demand.getFacultyId().trim().toUpperCase());
            }
        }

        classDao.saveOrUpdate(classGroup);
        return ResponseEntity.ok(Map.of("success", true, "message", "Class requirements saved successfully", "classGroup", classGroup));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<?> deleteClassGroup(@PathVariable String classId) {
        boolean deleted = classDao.deleteByClassId(classId.trim().toUpperCase());
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Class deleted successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Class not found"));
        }
    }
}
