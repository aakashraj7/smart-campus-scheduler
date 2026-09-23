package com.campus.scheduler.controller;

import com.campus.scheduler.dao.AcademicConfigDao;
import com.campus.scheduler.model.AcademicConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class AcademicConfigController {

    private final AcademicConfigDao configDao;

    public AcademicConfigController(AcademicConfigDao configDao) {
        this.configDao = configDao;
    }

    @GetMapping
    public ResponseEntity<AcademicConfig> getConfig() {
        return ResponseEntity.ok(configDao.getConfig());
    }

    @PutMapping
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, Object> body) {
        boolean includeSaturday = Boolean.parseBoolean(String.valueOf(body.get("includeSaturday")));
        int periodsPerDay = 6;
        if (body.get("periodsPerDay") != null) {
            periodsPerDay = Integer.parseInt(body.get("periodsPerDay").toString());
        }

        if (periodsPerDay < 1 || periodsPerDay > 10) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Periods per day must be between 1 and 10"));
        }

        AcademicConfig config = configDao.getConfig();
        config.setIncludeSaturday(includeSaturday);
        config.setPeriodsPerDay(periodsPerDay);
        configDao.saveConfig(config);

        return ResponseEntity.ok(Map.of("success", true, "message", "Academic configuration updated successfully", "config", config));
    }
}
