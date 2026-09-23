package com.campus.scheduler.controller;

import com.campus.scheduler.dao.FacultyDao;
import com.campus.scheduler.dao.UserDao;
import com.campus.scheduler.model.Faculty;
import com.campus.scheduler.model.User;
import com.campus.scheduler.util.PasswordUtil;
import com.campus.scheduler.util.ValidationUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserDao userDao;
    private final FacultyDao facultyDao;

    public AuthController(UserDao userDao, FacultyDao facultyDao) {
        this.userDao = userDao;
        this.facultyDao = facultyDao;
        ensureAdminExists();
    }

    private void ensureAdminExists() {
        if (userDao.countUsers() == 0) {
            User admin = new User("aakashraj", PasswordUtil.hashPassword("0000"), "Aakash Raj", "ADMIN", null);
            userDao.saveUser(admin);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username and password required"));
        }

        User user = userDao.findByUsername(username.trim());
        if (user == null || !PasswordUtil.verifyPassword(password.trim(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid username or password"));
        }

        session.setAttribute("user", user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("user", Map.of(
                "username", user.getUsername(),
                "name", user.getName(),
                "role", user.getRole(),
                "facultyId", user.getFacultyId() != null ? user.getFacultyId() : ""
        ));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "user", Map.of(
                        "username", user.getUsername(),
                        "name", user.getName(),
                        "role", user.getRole(),
                        "facultyId", user.getFacultyId() != null ? user.getFacultyId() : ""
                )
        ));
    }

    @PostMapping("/register-faculty")
    public ResponseEntity<?> registerFaculty(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String facultyId = (String) body.get("facultyId");
        String name = (String) body.get("name");
        String department = (String) body.get("department");
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");

        int maxPeriodsPerDay = body.get("maxPeriodsPerDay") != null ? Integer.parseInt(body.get("maxPeriodsPerDay").toString()) : 4;
        int maxPeriodsPerWeek = body.get("maxPeriodsPerWeek") != null ? Integer.parseInt(body.get("maxPeriodsPerWeek").toString()) : 20;

        // Validations
        if (!ValidationUtil.isValidCode(username)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username must be 2-20 alphanumeric characters"));
        }
        if (userDao.findByUsername(username.trim()) != null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username already exists"));
        }
        if (!ValidationUtil.isValidMinLength(password, 4)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password must be at least 4 characters"));
        }
        if (!ValidationUtil.isValidCode(facultyId)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Faculty ID must be alphanumeric (e.g. FAC001)"));
        }
        if (facultyDao.findByFacultyId(facultyId.trim().toUpperCase()) != null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Faculty ID already exists"));
        }
        if (!ValidationUtil.isValidMinLength(name, 2)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Faculty name cannot be empty"));
        }
        if (!ValidationUtil.isNonEmpty(department)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Department cannot be empty"));
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid email address format"));
        }
        if (!ValidationUtil.isValidMobile(phone)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mobile number must be a 10-digit number starting with 6, 7, 8, or 9"));
        }
        if (maxPeriodsPerDay < 1 || maxPeriodsPerDay > 8) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Max periods per day must be between 1 and 8"));
        }
        if (maxPeriodsPerWeek < 1 || maxPeriodsPerWeek > 40) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Max periods per week must be between 1 and 40"));
        }

        facultyId = facultyId.trim().toUpperCase();
        Faculty faculty = new Faculty(facultyId, name.trim(), department.trim().toUpperCase(), email.trim(), phone.trim(), maxPeriodsPerDay, maxPeriodsPerWeek);
        facultyDao.saveOrUpdate(faculty);

        User newUser = new User(username.trim(), PasswordUtil.hashPassword(password.trim()), name.trim(), "FACULTY", facultyId);
        userDao.saveUser(newUser);

        return ResponseEntity.ok(Map.of("success", true, "message", "Faculty account and profile created successfully", "faculty", faculty));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }
}
