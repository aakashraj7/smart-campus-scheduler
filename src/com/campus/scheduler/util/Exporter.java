package com.campus.scheduler.util;

import com.campus.scheduler.dao.SubjectDao;
import com.campus.scheduler.model.AcademicConfig;
import com.campus.scheduler.model.Subject;
import com.campus.scheduler.model.TimetableSlot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Exporter {

    private static final String EXPORT_DIR = "exports";

    public static String exportClassTimetableToCsv(String classId, List<TimetableSlot> slots, AcademicConfig config) throws IOException {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) dir.mkdirs();

        Map<String, String> shortNameMap = buildSubjectShortNameMap();

        String filename = EXPORT_DIR + "/Timetable_" + classId + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<String> days = config.getWorkingDays();
            int periods = config.getPeriodsPerDay();

            // Header
            writer.print("DAY");
            for (int p = 1; p <= periods; p++) {
                writer.print(",Period " + p);
            }
            writer.println();

            // Build grid
            Map<String, Map<Integer, String>> grid = new HashMap<>();
            for (String day : days) {
                grid.put(day.toUpperCase(), new HashMap<>());
            }
            for (TimetableSlot slot : slots) {
                Map<Integer, String> dayRow = grid.get(slot.getDay().toUpperCase());
                if (dayRow != null) {
                    String displaySubj = shortNameMap.getOrDefault(slot.getSubjectCode(), slot.getSubjectCode());
                    dayRow.put(slot.getPeriod(), displaySubj + " (" + slot.getFacultyId() + ")");
                }
            }

            // Write rows
            for (String day : days) {
                writer.print(day);
                Map<Integer, String> dayRow = grid.get(day.toUpperCase());
                for (int p = 1; p <= periods; p++) {
                    String val = (dayRow != null && dayRow.containsKey(p)) ? dayRow.get(p) : "FREE";
                    writer.print(",\"" + val + "\"");
                }
                writer.println();
            }
        }
        return filename;
    }

    public static String exportClassTimetableToTxt(String classId, List<TimetableSlot> slots, AcademicConfig config) throws IOException {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) dir.mkdirs();

        String filename = EXPORT_DIR + "/Timetable_" + classId + ".txt";
        String renderedTable = TableFormatter.renderClassTimetable(classId, slots, config);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.write(renderedTable);
        }
        return filename;
    }

    private static Map<String, String> buildSubjectShortNameMap() {
        Map<String, String> map = new HashMap<>();
        try {
            SubjectDao dao = new SubjectDao();
            for (Subject s : dao.getAllSubjects()) {
                if (s.getShortName() != null && !s.getShortName().trim().isEmpty()) {
                    map.put(s.getSubjectCode(), s.getShortName());
                }
            }
        } catch (Exception ignored) {}
        return map;
    }
}
