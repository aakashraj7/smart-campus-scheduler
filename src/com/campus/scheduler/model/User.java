package com.campus.scheduler.model;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String username;
    private String passwordHash;
    private String name;
    private String role; // "ADMIN" or "FACULTY"
    private String facultyId; // Optional link to Faculty profile
    private long createdAt;

    public User() {}

    public User(String username, String passwordHash, String name, String role, String facultyId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.facultyId = facultyId;
        this.createdAt = System.currentTimeMillis();
    }

    public static User fromDocument(Document doc) {
        if (doc == null) return null;
        User u = new User();
        u.id = doc.getObjectId("_id");
        u.username = doc.getString("username");
        u.passwordHash = doc.getString("passwordHash");
        u.name = doc.getString("name");
        u.role = doc.getString("role");
        u.facultyId = doc.getString("facultyId");
        Long created = doc.getLong("createdAt");
        u.createdAt = created != null ? created : 0L;
        return u;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("username", username);
        doc.put("passwordHash", passwordHash);
        doc.put("name", name);
        doc.put("role", role);
        doc.put("facultyId", facultyId);
        doc.put("createdAt", createdAt);
        return doc;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
