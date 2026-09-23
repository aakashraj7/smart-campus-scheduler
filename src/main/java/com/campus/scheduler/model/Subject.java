package com.campus.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Subject {
    private ObjectId id;
    private String subjectCode;
    private String name;
    private String shortName;
    private boolean isLab;

    public Subject() {}

    public Subject(String subjectCode, String name, String shortName, boolean isLab) {
        this.subjectCode = subjectCode;
        this.name = name;
        this.shortName = shortName;
        this.isLab = isLab;
    }

    public static Subject fromDocument(Document doc) {
        if (doc == null) return null;
        Subject s = new Subject();
        s.id = doc.getObjectId("_id");
        s.subjectCode = doc.getString("subjectCode");
        s.name = doc.getString("name");
        s.shortName = doc.getString("shortName");
        Boolean lab = doc.getBoolean("isLab");
        s.isLab = lab != null ? lab : false;
        return s;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("subjectCode", subjectCode);
        doc.put("name", name);
        doc.put("shortName", shortName);
        doc.put("isLab", isLab);
        return doc;
    }

    // Getters and Setters
    @JsonIgnore
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public boolean isLab() { return isLab; }
    public void setLab(boolean lab) { isLab = lab; }
}
