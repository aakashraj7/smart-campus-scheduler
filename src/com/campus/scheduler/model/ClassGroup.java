package com.campus.scheduler.model;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class ClassGroup {
    private ObjectId id;
    private String classId; // e.g. "CSE-3A"
    private String department; // e.g. "CSE"
    private int semester; // e.g. 3
    private List<SubjectDemand> subjectDemands = new ArrayList<>();

    public ClassGroup() {}

    public ClassGroup(String classId, String department, int semester) {
        this.classId = classId;
        this.department = department;
        this.semester = semester;
    }

    @SuppressWarnings("unchecked")
    public static ClassGroup fromDocument(Document doc) {
        if (doc == null) return null;
        ClassGroup cg = new ClassGroup();
        cg.id = doc.getObjectId("_id");
        cg.classId = doc.getString("classId");
        cg.department = doc.getString("department");
        Integer sem = doc.getInteger("semester");
        cg.semester = sem != null ? sem : 1;
        
        List<Document> demandDocs = (List<Document>) doc.get("subjectDemands");
        if (demandDocs != null) {
            for (Document d : demandDocs) {
                cg.subjectDemands.add(SubjectDemand.fromDocument(d));
            }
        }
        return cg;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("classId", classId);
        doc.put("department", department);
        doc.put("semester", semester);
        
        List<Document> demandDocs = new ArrayList<>();
        for (SubjectDemand sd : subjectDemands) {
            demandDocs.add(sd.toDocument());
        }
        doc.put("subjectDemands", demandDocs);
        return doc;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public List<SubjectDemand> getSubjectDemands() { return subjectDemands; }
    public void setSubjectDemands(List<SubjectDemand> subjectDemands) { this.subjectDemands = subjectDemands; }

    public void addSubjectDemand(SubjectDemand demand) {
        this.subjectDemands.add(demand);
    }
}
