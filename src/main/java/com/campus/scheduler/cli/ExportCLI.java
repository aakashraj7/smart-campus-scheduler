package com.campus.scheduler.cli;

import com.campus.scheduler.dao.*;
import com.campus.scheduler.model.*;
import com.campus.scheduler.util.Exporter;

import java.util.*;

public class ExportCLI {
    private final AcademicConfigDao configDao = new AcademicConfigDao();
    private final TimetableDao timetableDao = new TimetableDao();
    private final FacultyDao facultyDao = new FacultyDao();

    public void showExportMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n+-------------------------------------------------------------------+");
            System.out.println("|                EXPORTS & WORKLOAD REPORTS                         |");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.println("  [1] Export Class Timetable to Plain Text (.txt)");
            System.out.println("  [2] Export Class Timetable to Excel CSV (.csv)");
            System.out.println("  [3] View Faculty Workload Analytics Report");
            System.out.println("  -----------------------------------------------------------------");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.print("Select an option [0-3]: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                exportTxt(scanner);
            } else if (choice.equals("2")) {
                exportCsv(scanner);
            } else if (choice.equals("3")) {
                viewWorkloadAnalytics();
            } else if (choice.equals("0")) {
                return;
            } else {
                System.out.println("\n[ERROR] Invalid choice.");
            }
        }
    }

    private void exportTxt(Scanner scanner) {
        AcademicConfig config = configDao.getConfig();
        System.out.print("\nEnter Class ID to export (e.g. IT-C, CSE-3A): ");
        String classId = scanner.nextLine().trim().toUpperCase();

        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            System.out.println("\n[ERROR] No timetable found for Class " + classId);
            return;
        }

        try {
            String path = Exporter.exportClassTimetableToTxt(classId, tt.getSlots(), config);
            System.out.println("\n[OK] Class Timetable exported successfully to TXT file: " + path);
        } catch (Exception e) {
            System.out.println("\n[ERROR] Error exporting TXT file: " + e.getMessage());
        }
    }

    private void exportCsv(Scanner scanner) {
        AcademicConfig config = configDao.getConfig();
        System.out.print("\nEnter Class ID to export (e.g. IT-C, CSE-3A): ");
        String classId = scanner.nextLine().trim().toUpperCase();

        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            System.out.println("\n[ERROR] No timetable found for Class " + classId);
            return;
        }

        try {
            String path = Exporter.exportClassTimetableToCsv(classId, tt.getSlots(), config);
            System.out.println("\n[OK] Class Timetable exported successfully to CSV file: " + path);
        } catch (Exception e) {
            System.out.println("\n[ERROR] Error exporting CSV file: " + e.getMessage());
        }
    }

    private void viewWorkloadAnalytics() {
        List<Faculty> faculties = facultyDao.getAllFaculties();
        List<Timetable> timetables = timetableDao.getAllTimetables();

        if (faculties.isEmpty()) {
            System.out.println("\n[INFO] No faculties found in Database.");
            return;
        }

        Map<String, Integer> assignedMap = new HashMap<>();
        for (Timetable tt : timetables) {
            for (TimetableSlot slot : tt.getSlots()) {
                if (slot.getFacultyId() != null) {
                    assignedMap.put(slot.getFacultyId(), assignedMap.getOrDefault(slot.getFacultyId(), 0) + 1);
                }
            }
        }

        System.out.println("\n==========================================================================================");
        System.out.println("                           FACULTY WORKLOAD UTILIZATION REPORT");
        System.out.println("==========================================================================================");
        System.out.println(String.format("| %-10s | %-20s | %-12s | %-12s | %-15s |", "ID", "NAME", "ASSIGNED/WK", "MAX/WEEK", "UTILIZATION RATE"));
        System.out.println("------------------------------------------------------------------------------------------");

        for (Faculty f : faculties) {
            int assigned = assignedMap.getOrDefault(f.getFacultyId(), 0);
            int max = f.getMaxPeriodsPerWeek();
            double rate = max > 0 ? ((double) assigned / max) * 100.0 : 0.0;

            System.out.println(String.format("| %-10s | %-20s | %-12d | %-12d | %-14.1f%% |",
                    f.getFacultyId(), f.getName(), assigned, max, rate));
        }
        System.out.println("------------------------------------------------------------------------------------------\n");
    }
}
