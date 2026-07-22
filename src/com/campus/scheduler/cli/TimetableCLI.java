package com.campus.scheduler.cli;

import com.campus.scheduler.dao.*;
import com.campus.scheduler.engine.ConstraintChecker;
import com.campus.scheduler.engine.TimetableGenerator;
import com.campus.scheduler.model.*;
import com.campus.scheduler.util.TableFormatter;

import java.util.*;

public class TimetableCLI {
    private final AcademicConfigDao configDao = new AcademicConfigDao();
    private final ClassDao classDao = new ClassDao();
    private final FacultyDao facultyDao = new FacultyDao();
    private final TimetableDao timetableDao = new TimetableDao();

    public void showTimetableMenu(Scanner scanner, User currentUser) {
        while (true) {
            System.out.println("\n========================================================");
            System.out.println("             TIMETABLE GENERATOR & VIEWER");
            System.out.println("========================================================");
            if (currentUser.isAdmin()) {
                System.out.println("1. 🚀 Run Timetable Generation Engine (Auto-Schedule)");
                System.out.println("2. 🔄 Manual Period Swap / Override");
            }
            System.out.println("3. 📅 View Timetable by Class");
            System.out.println("4. 👨‍🏫 View Timetable by Faculty");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1") && currentUser.isAdmin()) {
                generateTimetables();
            } else if (choice.equals("2") && currentUser.isAdmin()) {
                manualSwap(scanner);
            } else if (choice.equals("3")) {
                viewByClass(scanner);
            } else if (choice.equals("4")) {
                viewByFaculty(scanner, currentUser);
            } else if (choice.equals("0")) {
                return;
            } else {
                System.out.println("❌ Invalid option or insufficient permissions.");
            }
        }
    }

    private void generateTimetables() {
        System.out.println("\n⏳ Initializing Intelligent Timetable Generator...");
        AcademicConfig config = configDao.getConfig();
        List<ClassGroup> classes = classDao.getAllClasses();
        List<Faculty> faculties = facultyDao.getAllFaculties();

        TimetableGenerator generator = new TimetableGenerator(config, classes, faculties);
        SchedulerResult result = generator.generate();

        if (result.isSuccess()) {
            System.out.println("\n========================================================");
            System.out.println("🎉 SUCCESS! Timetables generated successfully for " + result.getClassSchedules().size() + " classes!");
            System.out.println("========================================================");

            // Save to MongoDB
            for (Map.Entry<String, List<TimetableSlot>> entry : result.getClassSchedules().entrySet()) {
                String classId = entry.getKey();
                List<TimetableSlot> slots = entry.getValue();
                String ttId = "TT_" + classId + "_" + System.currentTimeMillis();

                Timetable tt = new Timetable(ttId, classId, slots);
                timetableDao.saveOrUpdate(tt);

                System.out.println(TableFormatter.renderClassTimetable(classId, slots, config));
            }
            System.out.println("✅ All timetables have been saved to MongoDB database!");
        } else {
            System.out.println("\n❌ ERROR: Unable to generate timetables due to constraint violations:");
            for (String err : result.getErrorMessages()) {
                System.out.println("  • " + err);
            }
        }
    }

    private void viewByClass(Scanner scanner) {
        AcademicConfig config = configDao.getConfig();
        List<Timetable> timetables = timetableDao.getAllTimetables();

        if (timetables.isEmpty()) {
            System.out.println("\nℹ️ No timetables generated yet. Please run the Auto-Schedule engine first.");
            return;
        }

        System.out.print("Enter Class ID to view (e.g., CSE-3A): ");
        String classId = scanner.nextLine().trim().toUpperCase();

        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            System.out.println("❌ No timetable found for Class " + classId);
        } else {
            System.out.println(TableFormatter.renderClassTimetable(classId, tt.getSlots(), config));
        }
    }

    private void viewByFaculty(Scanner scanner, User currentUser) {
        AcademicConfig config = configDao.getConfig();
        List<Timetable> timetables = timetableDao.getAllTimetables();

        if (timetables.isEmpty()) {
            System.out.println("\nℹ️ No timetables generated yet. Please run the Auto-Schedule engine first.");
            return;
        }

        String facultyId;
        if ("FACULTY".equalsIgnoreCase(currentUser.getRole()) && currentUser.getFacultyId() != null) {
            facultyId = currentUser.getFacultyId();
            System.out.println("Viewing schedule for your Faculty ID: " + facultyId);
        } else {
            System.out.print("Enter Faculty ID (e.g., FAC001): ");
            facultyId = scanner.nextLine().trim().toUpperCase();
        }

        Faculty faculty = facultyDao.findByFacultyId(facultyId);
        String facultyName = faculty != null ? faculty.getName() : facultyId;

        // Combine all class slots to show faculty's master schedule
        List<TimetableSlot> allSlots = new ArrayList<>();
        for (Timetable tt : timetables) {
            allSlots.addAll(tt.getSlots());
        }

        System.out.println(TableFormatter.renderFacultyTimetable(facultyId, facultyName, allSlots, config));
    }

    private void manualSwap(Scanner scanner) {
        AcademicConfig config = configDao.getConfig();
        System.out.print("Enter Class ID to swap periods in (e.g., CSE-3A): ");
        String classId = scanner.nextLine().trim().toUpperCase();

        Timetable tt = timetableDao.findByClassId(classId);
        if (tt == null) {
            System.out.println("❌ No timetable found for Class " + classId);
            return;
        }

        System.out.println("\n--- MANUAL PERIOD SWAP FOR " + classId + " ---");
        System.out.print("Enter Slot 1 Day (e.g. MONDAY): ");
        String day1 = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Slot 1 Period Number (1.." + config.getPeriodsPerDay() + "): ");
        int p1 = parsePositiveInt(scanner.nextLine().trim(), 1);

        System.out.print("Enter Slot 2 Day (e.g. WEDNESDAY): ");
        String day2 = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Slot 2 Period Number (1.." + config.getPeriodsPerDay() + "): ");
        int p2 = parsePositiveInt(scanner.nextLine().trim(), 1);

        TimetableSlot slot1 = findSlot(tt.getSlots(), day1, p1);
        TimetableSlot slot2 = findSlot(tt.getSlots(), day2, p2);

        if (slot1 == null && slot2 == null) {
            System.out.println("❌ Both target slots are empty. Nothing to swap.");
            return;
        }

        // Perform swap on object
        String sCode1 = slot1 != null ? slot1.getSubjectCode() : null;
        String fId1 = slot1 != null ? slot1.getFacultyId() : null;

        String sCode2 = slot2 != null ? slot2.getSubjectCode() : null;
        String fId2 = slot2 != null ? slot2.getFacultyId() : null;

        if (slot1 != null) {
            slot1.setSubjectCode(sCode2);
            slot1.setFacultyId(fId2);
        } else {
            tt.getSlots().add(new TimetableSlot(day1, p1, classId, sCode2, fId2));
        }

        if (slot2 != null) {
            slot2.setSubjectCode(sCode1);
            slot2.setFacultyId(fId1);
        } else {
            tt.getSlots().add(new TimetableSlot(day2, p2, classId, sCode1, fId1));
        }

        // Clean up empty slots
        tt.getSlots().removeIf(s -> s.getSubjectCode() == null);

        timetableDao.saveOrUpdate(tt);
        System.out.println("✅ Manual swap complete! Updated timetable saved to MongoDB.");
        System.out.println(TableFormatter.renderClassTimetable(classId, tt.getSlots(), config));
    }

    private TimetableSlot findSlot(List<TimetableSlot> slots, String day, int period) {
        for (TimetableSlot s : slots) {
            if (s.getDay().equalsIgnoreCase(day) && s.getPeriod() == period) {
                return s;
            }
        }
        return null;
    }

    private int parsePositiveInt(String input, int defaultVal) {
        try {
            int val = Integer.parseInt(input);
            return val > 0 ? val : defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
