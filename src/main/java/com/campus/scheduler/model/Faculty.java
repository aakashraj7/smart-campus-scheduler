package com.campus.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Faculty {
    private ObjectId id;
    private String facultyId;
    private String name;
    private String department;
    private String email;
    private String phone;
    private int maxPeriodsPerDay;
    private int maxPeriodsPerWeek;

    public Faculty() {}

    public Faculty(String facultyId, String name, String department, String email, String phone, int maxPeriodsPerDay, int maxPeriodsPerWeek) {
        this.facultyId = facultyId;
        this.name = name;
        this.department = department;
        this.email = email;
        this.phone = phone;
        this.maxPeriodsPerDay = maxPeriodsPerDay;
        this.maxPeriodsPerWeek = maxPeriodsPerWeek;
    }

    public static Faculty fromDocument(Document doc) {
        if (doc == null) return null;
        Faculty f = new Faculty();
        f.id = doc.getObjectId("_id");
        f.facultyId = doc.getString("facultyId");
        f.name = doc.getString("name");
        f.department = doc.getString("department");
        f.email = doc.getString("email");
        f.phone = doc.getString("phone");
        Integer daily = doc.getInteger("maxPeriodsPerDay");
        f.maxPeriodsPerDay = daily != null ? daily : 4;
        Integer weekly = doc.getInteger("maxPeriodsPerWeek");
        f.maxPeriodsPerWeek = weekly != null ? weekly : 20;
        return f;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("facultyId", facultyId);
        doc.put("name", name);
        doc.put("department", department);
        doc.put("email", email);
        doc.put("phone", phone);
        doc.put("maxPeriodsPerDay", maxPeriodsPerDay);
        doc.put("maxPeriodsPerWeek", maxPeriodsPerWeek);
        return doc;
    }

    // Getters and Setters
    @JsonIgnore
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getMaxPeriodsPerDay() { return maxPeriodsPerDay; }
    public void setMaxPeriodsPerDay(int maxPeriodsPerDay) { this.maxPeriodsPerDay = maxPeriodsPerDay; }

    public int getMaxPeriodsPerWeek() { return maxPeriodsPerWeek; }
    public void setMaxPeriodsPerWeek(int maxPeriodsPerWeek) { this.maxPeriodsPerWeek = maxPeriodsPerWeek; }
}
