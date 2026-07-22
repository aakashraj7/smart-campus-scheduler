package com.campus.scheduler.util;

import com.campus.scheduler.dao.SubjectDao;
import com.campus.scheduler.model.AcademicConfig;
import com.campus.scheduler.model.Subject;
import com.campus.scheduler.model.TimetableSlot;

import java.util.*;

public class TableFormatter {

    /**
     * Formats a Class Timetable into a neat ASCII grid table using Subject Short Names (e.g. OS, JAVA, DAA).
     */
    public static String renderClassTimetable(String classId, List<TimetableSlot> slots, AcademicConfig config) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================================================================\n");
        sb.append("                               TIMETABLE FOR CLASS: ").append(classId).append("\n");
        sb.append("========================================================================================\n");

        List<String> days = config.getWorkingDays();
        int periods = config.getPeriodsPerDay();

        Map<String, String> shortNameMap = buildSubjectShortNameMap();

        // Build grid map: Day -> Period -> "SHORT_NAME (FAC_ID)"
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

        // Header Row
        sb.append(String.format("| %-10s |", "DAY"));
        for (int p = 1; p <= periods; p++) {
            sb.append(String.format(" Period %-6d |", p));
        }
        sb.append("\n");

        // Separator
        sb.append("+------------+");
        for (int p = 1; p <= periods; p++) {
            sb.append("--------------+");
        }
        sb.append("\n");

        // Day Rows
        for (String day : days) {
            sb.append(String.format("| %-10s |", day));
            Map<Integer, String> dayRow = grid.get(day.toUpperCase());
            for (int p = 1; p <= periods; p++) {
                String val = (dayRow != null && dayRow.containsKey(p)) ? dayRow.get(p) : "---";
                sb.append(String.format(" %-12s |", truncate(val, 12)));
            }
            sb.append("\n");
        }

        sb.append("+------------+");
        for (int p = 1; p <= periods; p++) {
            sb.append("--------------+");
        }
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Formats a Faculty Timetable schedule view using Subject Short Names.
     */
    public static String renderFacultyTimetable(String facultyId, String facultyName, List<TimetableSlot> allSlots, AcademicConfig config) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================================================================\n");
        sb.append("                     FACULTY SCHEDULE: ").append(facultyName).append(" (").append(facultyId).append(")\n");
        sb.append("========================================================================================\n");

        List<String> days = config.getWorkingDays();
        int periods = config.getPeriodsPerDay();

        Map<String, String> shortNameMap = buildSubjectShortNameMap();

        Map<String, Map<Integer, String>> grid = new HashMap<>();
        for (String day : days) {
            grid.put(day.toUpperCase(), new HashMap<>());
        }

        int totalAssigned = 0;
        for (TimetableSlot slot : allSlots) {
            if (slot.getFacultyId().equalsIgnoreCase(facultyId)) {
                Map<Integer, String> dayRow = grid.get(slot.getDay().toUpperCase());
                if (dayRow != null) {
                    String displaySubj = shortNameMap.getOrDefault(slot.getSubjectCode(), slot.getSubjectCode());
                    dayRow.put(slot.getPeriod(), displaySubj + " [" + slot.getClassId() + "]");
                    totalAssigned++;
                }
            }
        }

        // Header Row
        sb.append(String.format("| %-10s |", "DAY"));
        for (int p = 1; p <= periods; p++) {
            sb.append(String.format(" Period %-6d |", p));
        }
        sb.append("\n");

        // Separator
        sb.append("+------------+");
        for (int p = 1; p <= periods; p++) {
            sb.append("--------------+");
        }
        sb.append("\n");

        // Day Rows
        for (String day : days) {
            sb.append(String.format("| %-10s |", day));
            Map<Integer, String> dayRow = grid.get(day.toUpperCase());
            for (int p = 1; p <= periods; p++) {
                String val = (dayRow != null && dayRow.containsKey(p)) ? dayRow.get(p) : "FREE";
                sb.append(String.format(" %-12s |", truncate(val, 12)));
            }
            sb.append("\n");
        }

        sb.append("+------------+");
        for (int p = 1; p <= periods; p++) {
            sb.append("--------------+");
        }
        sb.append("\n");
        sb.append(" Total Assigned Periods per Week: ").append(totalAssigned).append("\n");

        return sb.toString();
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

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}
