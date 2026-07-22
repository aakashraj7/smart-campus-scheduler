package com.campus.scheduler.cli;

import com.campus.scheduler.dao.FacultyDao;
import com.campus.scheduler.dao.UserDao;
import com.campus.scheduler.model.Faculty;
import com.campus.scheduler.model.User;
import com.campus.scheduler.util.PasswordUtil;
import com.campus.scheduler.util.ValidationUtil;

import java.util.Scanner;

public class AuthCLI {
    private final UserDao userDao = new UserDao();
    private final FacultyDao facultyDao = new FacultyDao();
    private User currentUser = null;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean checkInitialAdminSetup(Scanner scanner) {
        if (userDao.countUsers() == 0) {
            System.out.println("\n********************************************************");
            System.out.println("  WELCOME TO SMART CAMPUS SCHEDULER INITIAL SETUP");
            System.out.println("  No admin account found. Let's create an Administrator!");
            System.out.println("********************************************************");

            String username;
            while (true) {
                System.out.print("Enter Admin Username (alphanumeric, e.g. admin): ");
                username = scanner.nextLine().trim();
                if (ValidationUtil.isValidCode(username)) break;
                System.out.println("❌ Invalid Username. Must be 2-20 alphanumeric characters without spaces.");
            }

            String name;
            while (true) {
                System.out.print("Enter Admin Full Name: ");
                name = scanner.nextLine().trim();
                if (ValidationUtil.isValidMinLength(name, 2)) break;
                System.out.println("❌ Name cannot be empty.");
            }

            String password;
            while (true) {
                System.out.print("Enter Admin Password (min 4 chars): ");
                password = scanner.nextLine().trim();
                if (ValidationUtil.isValidMinLength(password, 4)) break;
                System.out.println("❌ Password must be at least 4 characters long.");
            }

            User admin = new User(username, PasswordUtil.hashPassword(password), name, "ADMIN", null);
            userDao.saveUser(admin);
            System.out.println("✅ Administrator account '" + username + "' created successfully!");
            this.currentUser = admin;
            return true;
        }
        return false;
    }

    public boolean login(Scanner scanner) {
        System.out.println("\n--------------------------------------------------------");
        System.out.println("                   USER LOGIN");
        System.out.println("--------------------------------------------------------");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.findByUsername(username);
        if (user == null) {
            System.out.println("❌ Invalid credentials: User not found.");
            return false;
        }

        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            this.currentUser = user;
            System.out.println("\n🎉 Login Successful! Welcome " + user.getName() + " [" + user.getRole() + "]");
            return true;
        } else {
            System.out.println("❌ Invalid credentials: Password incorrect.");
            return false;
        }
    }

    /**
     * Unified Faculty Account & Profile Registration:
     * Asks for all details (login credentials + faculty profile + contact info + workload limits) in a single flow.
     */
    public void registerFacultyUser(Scanner scanner) {
        System.out.println("\n--------------------------------------------------------");
        System.out.println("     UNIFIED FACULTY REGISTRATION & PROFILE CREATION");
        System.out.println("--------------------------------------------------------");

        // 1. Username
        String username;
        while (true) {
            System.out.print("Enter Login Username (e.g., smith_fac): ");
            username = scanner.nextLine().trim();
            if (!ValidationUtil.isValidCode(username)) {
                System.out.println("❌ Invalid Username format. Use 2-20 alphanumeric characters.");
                continue;
            }
            if (userDao.findByUsername(username) != null) {
                System.out.println("❌ Error: Username already exists.");
                continue;
            }
            break;
        }

        // 2. Password
        String password;
        while (true) {
            System.out.print("Enter Login Password (min 4 chars): ");
            password = scanner.nextLine().trim();
            if (ValidationUtil.isValidMinLength(password, 4)) break;
            System.out.println("❌ Password must be at least 4 characters long.");
        }

        // 3. Faculty ID
        String facultyId;
        while (true) {
            System.out.print("Enter Unique Faculty ID (e.g., FAC001): ");
            facultyId = scanner.nextLine().trim().toUpperCase();
            if (!ValidationUtil.isValidCode(facultyId)) {
                System.out.println("❌ Invalid Faculty ID format.");
                continue;
            }
            if (facultyDao.findByFacultyId(facultyId) != null) {
                System.out.println("❌ Error: Faculty ID already exists in Database.");
                continue;
            }
            break;
        }

        // 4. Full Name
        String name;
        while (true) {
            System.out.print("Enter Faculty Full Name (e.g., Dr. Smith): ");
            name = scanner.nextLine().trim();
            if (ValidationUtil.isValidMinLength(name, 2)) break;
            System.out.println("❌ Name cannot be empty.");
        }

        // 5. Department
        String dept;
        while (true) {
            System.out.print("Enter Department (e.g., CSE): ");
            dept = scanner.nextLine().trim().toUpperCase();
            if (ValidationUtil.isNonEmpty(dept)) break;
            System.out.println("❌ Department cannot be empty.");
        }

        // 6. Email
        String email;
        while (true) {
            System.out.print("Enter Email Address (e.g., smith@campus.edu): ");
            email = scanner.nextLine().trim();
            if (ValidationUtil.isValidEmail(email)) break;
            System.out.println("❌ Invalid Email Address format! Example: dr.smith@campus.edu");
        }

        // 7. Mobile Number
        String phone;
        while (true) {
            System.out.print("Enter Mobile Number (10 digits starting with 6-9, e.g. 9876543210): ");
            phone = scanner.nextLine().trim();
            if (ValidationUtil.isValidMobile(phone)) break;
            System.out.println("❌ Invalid Mobile Number! Standard 10-digit number starting with 6, 7, 8, or 9 is required.");
        }

        // 8. Max Periods Per Day
        int maxDay;
        while (true) {
            System.out.print("Enter Max Periods Per Day (1 to 8, e.g. 3): ");
            String input = scanner.nextLine().trim();
            if (ValidationUtil.isValidIntRange(input, 1, 8)) {
                maxDay = Integer.parseInt(input);
                break;
            }
            System.out.println("❌ Invalid input! Must be a number between 1 and 8.");
        }

        // 9. Max Periods Per Week
        int maxWeek;
        while (true) {
            System.out.print("Enter Max Periods Per Week (1 to 40, e.g. 15): ");
            String input = scanner.nextLine().trim();
            if (ValidationUtil.isValidIntRange(input, 1, 40)) {
                maxWeek = Integer.parseInt(input);
                break;
            }
            System.out.println("❌ Invalid input! Must be a number between 1 and 40.");
        }

        // Save both Faculty Profile & User Account
        Faculty faculty = new Faculty(facultyId, name, dept, email, phone, maxDay, maxWeek);
        facultyDao.saveOrUpdate(faculty);

        User newUser = new User(username, PasswordUtil.hashPassword(password), name, "FACULTY", facultyId);
        userDao.saveUser(newUser);

        System.out.println("\n🎉 SUCCESS! Faculty Account & Profile Created in 1 Unified Step:");
        System.out.println(" • Login Username : " + username);
        System.out.println(" • Faculty ID      : " + facultyId);
        System.out.println(" • Name            : " + name + " (" + dept + ")");
        System.out.println(" • Contact         : Email: " + email + " | Mobile: " + phone);
        System.out.println(" • Workload Limit  : " + maxDay + " periods/day | " + maxWeek + " periods/week");
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 Logged out user: " + currentUser.getUsername());
            currentUser = null;
        }
    }
}
