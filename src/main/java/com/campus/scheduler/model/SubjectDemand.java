package com.campus.scheduler.model;

import org.bson.Document;

public class SubjectDemand {
    private String subjectCode;
    private String facultyId;
    private int periodsPerWeek;

    public SubjectDemand() {}

    public SubjectDemand(String subjectCode, String facultyId, int periodsPerWeek) {
        this.subjectCode = subjectCode;
        this.facultyId = facultyId;
        this.periodsPerWeek = periodsPerWeek;
    }

    public static SubjectDemand fromDocument(Document doc) {
        if (doc == null) return null;
        SubjectDemand sd = new SubjectDemand();
        sd.subjectCode = doc.getString("subjectCode");
        sd.facultyId = doc.getString("facultyId");
        Integer p = doc.getInteger("periodsPerWeek");
        sd.periodsPerWeek = p != null ? p : 0;
        return sd;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.put("subjectCode", subjectCode);
        doc.put("facultyId", facultyId);
        doc.put("periodsPerWeek", periodsPerWeek);
        return doc;
    }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public int getPeriodsPerWeek() { return periodsPerWeek; }
    public void setPeriodsPerWeek(int periodsPerWeek) { this.periodsPerWeek = periodsPerWeek; }
}
