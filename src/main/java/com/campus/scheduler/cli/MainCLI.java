package com.campus.scheduler.cli;

import com.campus.scheduler.config.DatabaseConfig;
import com.campus.scheduler.model.User;

import java.util.Scanner;

public class MainCLI {
    private final AuthCLI authCLI = new AuthCLI();
    private final AcademicSetupCLI setupCLI = new AcademicSetupCLI();
    private final TimetableCLI timetableCLI = new TimetableCLI();
    private final ExportCLI exportCLI = new ExportCLI();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        printBanner();

        try {
            // Step 1: Initial Admin Creation if DB is fresh
            authCLI.checkInitialAdminSetup(scanner);

            // Step 2: Main Application Menu Loop
            while (true) {
                if (authCLI.getCurrentUser() == null) {
                    if (!authCLI.login(scanner)) {
                        System.out.print("\nDo you want to try logging in again? (y/n): ");
                        String ans = scanner.nextLine().trim();
                        if (!ans.equalsIgnoreCase("y")) {
                            System.out.println("\nExiting Smart Campus Scheduler. Goodbye!");
                            break;
                        }
                        continue;
                    }
                }

                User user = authCLI.getCurrentUser();
                System.out.println("\n+-------------------------------------------------------------------+");
                System.out.println("|                 SMART CAMPUS SCHEDULER - MAIN MENU                |");
                System.out.println("+-------------------------------------------------------------------+");
                System.out.println("  User: " + user.getName() + " (" + user.getUsername() + ") | Role: " + user.getRole());
                System.out.println("+-------------------------------------------------------------------+");
                
                if (user.isAdmin()) {
                    System.out.println("  [1] Module 2: Academic Setup & Entity Management");
                    System.out.println("  [2] Module 3 & 4: Timetable Generator, Viewer & Manual Swap");
                    System.out.println("  [3] Module 5: Exports (TXT / CSV) & Workload Analytics");
                    System.out.println("  [4] Register New Faculty Account (Unified Profile Setup)");
                } else {
                    System.out.println("  [2] View Timetables (Class & Personal Faculty Schedule)");
                    System.out.println("  [3] Export Timetables & View Workload Analytics");
                }
                
                System.out.println("  -----------------------------------------------------------------");
                System.out.println("  [9] Logout / Switch Account");
                System.out.println("  [0] Exit Application");
                System.out.println("+-------------------------------------------------------------------+");
                System.out.print("Select an option [0-9]: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        if (user.isAdmin()) {
                            setupCLI.showSetupMenu(scanner);
                        } else {
                            System.out.println("\n[ERROR] Access Denied: Only Admin/HOD can access Academic Setup.");
                        }
                        break;
                    case "2":
                        timetableCLI.showTimetableMenu(scanner, user);
                        break;
                    case "3":
                        exportCLI.showExportMenu(scanner);
                        break;
                    case "4":
                        if (user.isAdmin()) {
                            authCLI.registerFacultyUser(scanner);
                        } else {
                            System.out.println("\n[ERROR] Access Denied: Only Admin/HOD can register new accounts.");
                        }
                        break;
                    case "9":
                        authCLI.logout();
                        break;
                    case "0":
                        System.out.println("\nThank you for using Smart Campus Scheduler!");
                        DatabaseConfig.closeConnection();
                        return;
                    default:
                        System.out.println("\n[ERROR] Invalid option. Please select a valid number.");
                }
            }
        } catch (Exception e) {
            System.err.println("\n[FATAL ERROR] " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConfig.closeConnection();
            scanner.close();
        }
    }

    private void printBanner() {
        System.out.println("+--------------------------------------------------------------------+");
        System.out.println("|   _____ __  ___ ___   ____  ______   ______ ___     __  ___ ____   |");
        System.out.println("|  / ___//  |/  //   | / __ \\/_  __/  / ____//   |   /  |/  // __ \\  |");
        System.out.println("|  \\__ \\/ /|_/ // /| |/ /_/ / / /    / /    / /| |  / /|_/ // /_/ /  |");
        System.out.println("| ___/ / /  / // ___ / _, _/ / /    / /___ / ___ | / /  / // ____/   |");
        System.out.println("|/____/_/  /_//_/  |_/_/ |_| /_/     \\____//_/  |_/_/  /_//_/        |");
        System.out.println("|                                                                    |");
        System.out.println("|             INTELLIGENT CAMPUS TIMETABLE SCHEDULER                 |");
        System.out.println("|             Pure Java CLI + MongoDB Local Storage                  |");
        System.out.println("+--------------------------------------------------------------------+");
    }
}
