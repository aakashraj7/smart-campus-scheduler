package com.campus.scheduler.controller;

import com.campus.scheduler.dao.SubjectDao;
import com.campus.scheduler.model.Subject;
import com.campus.scheduler.util.ValidationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = "*")
public class SubjectController {

    private final SubjectDao subjectDao;

    public SubjectController(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectDao.getAllSubjects();
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getSubject(@PathVariable String code) {
        Subject s = subjectDao.findByCode(code.trim().toUpperCase());
        if (s == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(s);
    }

    @PostMapping
    public ResponseEntity<?> saveSubject(@RequestBody Subject subject) {
        if (!ValidationUtil.isValidCode(subject.getSubjectCode())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Subject code must be 2-20 alphanumeric characters"));
        }
        if (!ValidationUtil.isValidMinLength(subject.getName(), 2)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Subject name is required"));
        }
        if (!ValidationUtil.isNonEmpty(subject.getShortName())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Short name / Abbreviation is required (e.g. OS, JAVA)"));
        }

        subject.setSubjectCode(subject.getSubjectCode().trim().toUpperCase());
        subject.setName(subject.getName().trim());
        subject.setShortName(subject.getShortName().trim().toUpperCase());

        subjectDao.saveOrUpdate(subject);
        return ResponseEntity.ok(Map.of("success", true, "message", "Subject saved successfully", "subject", subject));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteSubject(@PathVariable String code) {
        boolean deleted = subjectDao.deleteByCode(code.trim().toUpperCase());
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Subject deleted successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Subject not found"));
        }
    }
}
