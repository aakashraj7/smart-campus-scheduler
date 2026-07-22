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

            // Step 2: Login loop
            while (true) {
                if (authCLI.getCurrentUser() == null) {
                    if (!authCLI.login(scanner)) {
                        System.out.println("Do you want to try logging in again? (y/n): ");
                        String ans = scanner.nextLine().trim();
                        if (!ans.equalsIgnoreCase("y")) {
                            System.out.println("Exiting Smart Campus Scheduler. Goodbye!");
                            break;
                        }
                        continue;
                    }
                }

                User user = authCLI.getCurrentUser();
                System.out.println("\n========================================================");
                System.out.println("        SMART CAMPUS SCHEDULER - MAIN MENU");
                System.out.println("========================================================");
                System.out.println("Logged in as: " + user.getName() + " (" + user.getUsername() + ") | Role: " + user.getRole());
                System.out.println("--------------------------------------------------------");
                
                if (user.isAdmin()) {
                    System.out.println("1. ⚙️  Module 2: Academic Setup & Entity Data Entry");
                    System.out.println("2. 🗓️  Module 3 & 4: Timetable Generator & Viewer / Swap");
                    System.out.println("3. 📤  Module 5: Exports (TXT/CSV) & Workload Reports");
                    System.out.println("4. 👤  Register New Faculty User Account");
                } else {
                    System.out.println("2. 🗓️  View Timetables (Class & Personal Schedule)");
                    System.out.println("3. 📤  Export Timetables & View Reports");
                }
                
                System.out.println("9. 🚪 Logout / Switch Account");
                System.out.println("0. ❌ Exit Application");
                System.out.print("Select an option: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        if (user.isAdmin()) {
                            setupCLI.showSetupMenu(scanner);
                        } else {
                            System.out.println("❌ Only Admin/HOD can access Academic Setup.");
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
                            System.out.println("❌ Only Admin/HOD can register new user accounts.");
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
                        System.out.println("❌ Invalid option. Please select a valid number.");
                }
            }
        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConfig.closeConnection();
            scanner.close();
        }
    }

    private void printBanner() {
        System.out.println("========================================================================");
        System.out.println("   _____ __  ___ ___   ____  ______   ______ ___     __  ___ ____   __  ");
        System.out.println("  / ___//  |/  //   | / __ \\/_  __/  / ____//   |   /  |/  // __ \\ / /  ");
        System.out.println("  \\__ \\/ /|_/ // /| |/ /_/ / / /    / /    / /| |  / /|_/ // /_/ // /   ");
        System.out.println(" ___/ / /  / // ___ / _, _/ / /    / /___ / ___ | / /  / // ____// /___ ");
        System.out.println("/____/_/  /_//_/  |_/_/ |_| /_/     \\____//_/  |_/_/  /_//_/    /_____/ ");
        System.out.println("                                                                        ");
        System.out.println("              INTELLIGENT CAMPUS TIMETABLE SCHEDULER                    ");
        System.out.println("              Pure Java CLI + MongoDB Local Storage                     ");
        System.out.println("========================================================================");
    }
}
