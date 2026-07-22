package com.campus.scheduler.model;

import org.bson.Document;

public class TimetableSlot {
    private String day; // e.g. "MONDAY"
    private int period; // e.g. 1..6
    private String classId; // e.g. "CSE-3A"
    private String subjectCode; // e.g. "CS301"
    private String facultyId; // e.g. "FAC001"

    public TimetableSlot() {}

    public TimetableSlot(String day, int period, String classId, String subjectCode, String facultyId) {
        this.day = day;
        this.period = period;
        this.classId = classId;
        this.subjectCode = subjectCode;
        this.facultyId = facultyId;
    }

    public static TimetableSlot fromDocument(Document doc) {
        if (doc == null) return null;
        TimetableSlot slot = new TimetableSlot();
        slot.day = doc.getString("day");
        Integer p = doc.getInteger("period");
        slot.period = p != null ? p : 0;
        slot.classId = doc.getString("classId");
        slot.subjectCode = doc.getString("subjectCode");
        slot.facultyId = doc.getString("facultyId");
        return slot;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.put("day", day);
        doc.put("period", period);
        doc.put("classId", classId);
        doc.put("subjectCode", subjectCode);
        doc.put("facultyId", facultyId);
        return doc;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }
}
