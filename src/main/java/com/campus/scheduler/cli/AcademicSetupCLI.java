package com.campus.scheduler.cli;

import com.campus.scheduler.dao.*;
import com.campus.scheduler.model.*;
import com.campus.scheduler.util.ValidationUtil;

import java.util.*;

public class AcademicSetupCLI {
    private final FacultyDao facultyDao = new FacultyDao();
    private final SubjectDao subjectDao = new SubjectDao();
    private final ClassDao classDao = new ClassDao();
    private final AcademicConfigDao configDao = new AcademicConfigDao();
    private final AuthCLI authCLI = new AuthCLI();

    public void showSetupMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n+-------------------------------------------------------------------+");
            System.out.println("|                MODULE 2: ACADEMIC SETUP & DATA ENTRY              |");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.println("  [1] Manage Faculties (Workload Limits & Unified Profile)");
            System.out.println("  [2] Manage Subjects Catalogue");
            System.out.println("  [3] Configure Class Subject Demands (e.g. 8 OS, 8 JAVA, 8 DAA)");
            System.out.println("  [4] Working Days & Periods Configuration (Saturday Toggle)");
            System.out.println("  -----------------------------------------------------------------");
            System.out.println("  [0] Back to Main Menu");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.print("Select an option [0-4]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    manageFaculties(scanner);
                    break;
                case "2":
                    manageSubjects(scanner);
                    break;
                case "3":
                    manageClassRequirements(scanner);
                    break;
                case "4":
                    configureAcademicDays(scanner);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("\n[ERROR] Invalid option. Please select a valid number.");
            }
        }
    }

    private void manageFaculties(Scanner scanner) {
        while (true) {
            System.out.println("\n+-------------------------------------------------------------------+");
            System.out.println("|                         FACULTY MANAGEMENT                        |");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.println("  [1] Register / Add New Faculty (Unified Profile & Account Setup)");
            System.out.println("  [2] View All Faculties");
            System.out.println("  [3] Delete Faculty");
            System.out.println("  -----------------------------------------------------------------");
            System.out.println("  [0] Back");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.print("Select choice [0-3]: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                authCLI.registerFacultyUser(scanner);
            } else if (choice.equals("2")) {
                List<Faculty> list = facultyDao.getAllFaculties();
                if (list.isEmpty()) {
                    System.out.println("\n[INFO] No faculties added yet.");
                } else {
                    System.out.println("\n---------------------------------------------------------------------------------------------------------");
                    System.out.println(String.format("| %-10s | %-18s | %-8s | %-22s | %-12s | %-7s | %-8s |", "ID", "NAME", "DEPT", "EMAIL", "PHONE", "MAX/DAY", "MAX/WEEK"));
                    System.out.println("---------------------------------------------------------------------------------------------------------");
                    for (Faculty f : list) {
                        System.out.println(String.format("| %-10s | %-18s | %-8s | %-22s | %-12s | %-7d | %-8d |",
                                f.getFacultyId(), f.getName(), f.getDepartment(), 
                                truncate(f.getEmail(), 22), f.getPhone() != null ? f.getPhone() : "N/A", 
                                f.getMaxPeriodsPerDay(), f.getMaxPeriodsPerWeek()));
                    }
                    System.out.println("---------------------------------------------------------------------------------------------------------");
                }
            } else if (choice.equals("3")) {
                System.out.print("\nEnter Faculty ID to delete: ");
                String facultyId = scanner.nextLine().trim().toUpperCase();
                if (facultyDao.deleteByFacultyId(facultyId)) {
                    System.out.println("\n[OK] Faculty " + facultyId + " deleted successfully.");
                } else {
                    System.out.println("\n[ERROR] Faculty ID not found.");
                }
            } else if (choice.equals("0")) {
                break;
            }
        }
    }

    private void manageSubjects(Scanner scanner) {
        while (true) {
            System.out.println("\n+-------------------------------------------------------------------+");
            System.out.println("|                         SUBJECT MANAGEMENT                        |");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.println("  [1] Add / Edit Subject");
            System.out.println("  [2] View All Subjects");
            System.out.println("  [3] Delete Subject");
            System.out.println("  -----------------------------------------------------------------");
            System.out.println("  [0] Back");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.print("Select choice [0-3]: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                String code;
                while (true) {
                    System.out.print("Enter Subject Code (e.g., CS301): ");
                    code = scanner.nextLine().trim().toUpperCase();
                    if (ValidationUtil.isValidCode(code)) break;
                    System.out.println("[ERROR] Invalid Subject Code! Must be 2-20 alphanumeric characters without spaces.");
                }

                String name;
                while (true) {
                    System.out.print("Enter Subject Full Name (e.g., Database Management Systems): ");
                    name = scanner.nextLine().trim();
                    if (ValidationUtil.isValidMinLength(name, 2)) break;
                    System.out.println("[ERROR] Subject name cannot be empty.");
                }

                String shortName;
                while (true) {
                    System.out.print("Enter Short Name / Abbreviation (e.g., DBMS): ");
                    shortName = scanner.nextLine().trim().toUpperCase();
                    if (ValidationUtil.isNonEmpty(shortName)) break;
                    System.out.println("[ERROR] Short name cannot be empty.");
                }

                System.out.print("Is this a Lab/Practical? (y/n): ");
                boolean isLab = scanner.nextLine().trim().equalsIgnoreCase("y");

                Subject s = new Subject(code, name, shortName, isLab);
                subjectDao.saveOrUpdate(s);
                System.out.println("\n[OK] Subject '" + name + "' (" + code + ") saved successfully!");
            } else if (choice.equals("2")) {
                List<Subject> list = subjectDao.getAllSubjects();
                if (list.isEmpty()) {
                    System.out.println("\n[INFO] No subjects added yet.");
                } else {
                    System.out.println("\n-------------------------------------------------------------");
                    System.out.println(String.format("| %-10s | %-25s | %-10s | %-5s |", "CODE", "NAME", "SHORT", "LAB"));
                    System.out.println("-------------------------------------------------------------");
                    for (Subject s : list) {
                        System.out.println(String.format("| %-10s | %-25s | %-10s | %-5s |",
                                s.getSubjectCode(), truncate(s.getName(), 25), s.getShortName(), s.isLab() ? "YES" : "NO"));
                    }
                    System.out.println("-------------------------------------------------------------");
                }
            } else if (choice.equals("3")) {
                System.out.print("\nEnter Subject Code to delete: ");
                String code = scanner.nextLine().trim().toUpperCase();
                if (subjectDao.deleteByCode(code)) {
                    System.out.println("\n[OK] Subject " + code + " deleted.");
                } else {
                    System.out.println("\n[ERROR] Subject Code not found.");
                }
            } else if (choice.equals("0")) {
                break;
            }
        }
    }

    private void manageClassRequirements(Scanner scanner) {
        while (true) {
            System.out.println("\n+-------------------------------------------------------------------+");
            System.out.println("|                CLASS REQUIREMENTS CONFIGURATION                   |");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.println("  [1] Configure / Edit Subject Demands for a Class");
            System.out.println("  [2] View All Class Demands");
            System.out.println("  [3] Delete Class Configuration");
            System.out.println("  -----------------------------------------------------------------");
            System.out.println("  [0] Back");
            System.out.println("+-------------------------------------------------------------------+");
            System.out.print("Select choice [0-3]: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                String classId;
                while (true) {
                    System.out.print("Enter Class ID / Section (e.g., CSE-3A): ");
                    classId = scanner.nextLine().trim().toUpperCase();
                    if (ValidationUtil.isValidCode(classId)) break;
                    System.out.println("[ERROR] Invalid Class ID! Use alphanumeric characters (e.g., CSE-3A).");
                }

                String dept;
                while (true) {
                    System.out.print("Enter Department (e.g., CSE): ");
                    dept = scanner.nextLine().trim().toUpperCase();
                    if (ValidationUtil.isNonEmpty(dept)) break;
                    System.out.println("[ERROR] Department cannot be empty.");
                }

                int sem;
                while (true) {
                    System.out.print("Enter Semester (1 to 10): ");
                    String input = scanner.nextLine().trim();
                    if (ValidationUtil.isValidIntRange(input, 1, 10)) {
                        sem = Integer.parseInt(input);
                        break;
                    }
                    System.out.println("[ERROR] Invalid Semester! Enter a number between 1 and 10.");
                }

                ClassGroup classGroup = new ClassGroup(classId, dept, sem);

                System.out.println("\nAssign Subject Period Requirements for Class " + classId + ":");
                while (true) {
                    System.out.print("Enter Subject Code (or type 'done' to finish): ");
                    String subCode = scanner.nextLine().trim().toUpperCase();
                    if (subCode.equalsIgnoreCase("DONE")) break;

                    if (!ValidationUtil.isValidCode(subCode)) {
                        System.out.println("[ERROR] Invalid Subject Code format.");
                        continue;
                    }

                    Subject s = subjectDao.findByCode(subCode);
                    if (s == null) {
                        System.out.println("[WARN] Subject '" + subCode + "' not found in Database. Please make sure to add it under Subjects menu first.");
                    }

                    String facId;
                    while (true) {
                        System.out.print("Enter Assigned Faculty ID (e.g., FAC001): ");
                        facId = scanner.nextLine().trim().toUpperCase();
                        if (ValidationUtil.isValidCode(facId)) break;
                        System.out.println("[ERROR] Invalid Faculty ID format.");
                    }

                    Faculty f = facultyDao.findByFacultyId(facId);
                    if (f == null) {
                        System.out.println("[WARN] Faculty '" + facId + "' not found in Database. Please make sure to add them under Faculty menu first.");
                    }

                    int periods;
                    while (true) {
                        System.out.print("Enter Periods Needed Per Week (1 to 20, e.g. 8 for OS): ");
                        String input = scanner.nextLine().trim();
                        if (ValidationUtil.isValidIntRange(input, 1, 20)) {
                            periods = Integer.parseInt(input);
                            break;
                        }
                        System.out.println("[ERROR] Invalid period count! Enter a number between 1 and 20.");
                    }

                    classGroup.addSubjectDemand(new SubjectDemand(subCode, facId, periods));
                    System.out.println("  [+] Added requirement: " + periods + " periods of " + subCode + " taught by Faculty " + facId);
                }

                classDao.saveOrUpdate(classGroup);
                System.out.println("\n[OK] Saved class requirements for Class " + classId);
            } else if (choice.equals("2")) {
                List<ClassGroup> list = classDao.getAllClasses();
                if (list.isEmpty()) {
                    System.out.println("\n[INFO] No class requirements configured yet.");
                } else {
                    for (ClassGroup cg : list) {
                        System.out.println("\nClass: " + cg.getClassId() + " (Dept: " + cg.getDepartment() + ", Semester " + cg.getSemester() + ")");
                        System.out.println("Subject Demands:");
                        int total = 0;
                        for (SubjectDemand sd : cg.getSubjectDemands()) {
                            System.out.println("  - " + sd.getSubjectCode() + " | Faculty: " + sd.getFacultyId() + " | " + sd.getPeriodsPerWeek() + " periods/week");
                            total += sd.getPeriodsPerWeek();
                        }
                        System.out.println(" Total Weekly Periods Requested: " + total);
                    }
                }
            } else if (choice.equals("3")) {
                System.out.print("\nEnter Class ID to delete: ");
                String classId = scanner.nextLine().trim().toUpperCase();
                if (classDao.deleteByClassId(classId)) {
                    System.out.println("\n[OK] Class " + classId + " configuration deleted.");
                } else {
                    System.out.println("\n[ERROR] Class ID not found.");
                }
            } else if (choice.equals("0")) {
                break;
            }
        }
    }

    private void configureAcademicDays(Scanner scanner) {
        AcademicConfig config = configDao.getConfig();
        System.out.println("\n+-------------------------------------------------------------------+");
        System.out.println("|               WORKING DAYS & PERIODS CONFIGURATION                |");
        System.out.println("+-------------------------------------------------------------------+");
        System.out.println("  Current Setting:");
        System.out.println("  Include Saturday : " + (config.isIncludeSaturday() ? "YES (Mon-Sat)" : "NO (Mon-Fri)"));
        System.out.println("  Periods Per Day  : " + config.getPeriodsPerDay());
        System.out.println("  Total Slots/Week : " + config.getTotalSlotsPerClass());
        System.out.println("+-------------------------------------------------------------------+");

        System.out.print("Include Saturday in schedule? (y/n): ");
        String satChoice = scanner.nextLine().trim();
        boolean includeSaturday = satChoice.equalsIgnoreCase("y");

        int periods;
        while (true) {
            System.out.print("Enter number of periods per day (1 to 10, e.g. 5, 6, 8): ");
            String input = scanner.nextLine().trim();
            if (ValidationUtil.isValidIntRange(input, 1, 10)) {
                periods = Integer.parseInt(input);
                break;
            }
            System.out.println("[ERROR] Invalid periods count! Must be between 1 and 10.");
        }

        config.setIncludeSaturday(includeSaturday);
        config.setPeriodsPerDay(periods);
        configDao.saveConfig(config);

        System.out.println("\n[OK] Configuration updated successfully!");
        System.out.println(" Working Days : " + String.join(", ", config.getWorkingDays()));
        System.out.println(" Total Capacity per Class: " + config.getTotalSlotsPerClass() + " slots/week");
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}
