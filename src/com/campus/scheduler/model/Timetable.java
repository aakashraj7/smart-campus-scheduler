package com.campus.scheduler.model;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class Timetable {
    private ObjectId id;
    private String timetableId;
    private String classId;
    private String status; // "DRAFT" or "PUBLISHED"
    private List<TimetableSlot> slots = new ArrayList<>();
    private long generatedAt;

    public Timetable() {
        this.generatedAt = System.currentTimeMillis();
        this.status = "PUBLISHED";
    }

    public Timetable(String timetableId, String classId, List<TimetableSlot> slots) {
        this.timetableId = timetableId;
        this.classId = classId;
        this.slots = slots;
        this.status = "PUBLISHED";
        this.generatedAt = System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    public static Timetable fromDocument(Document doc) {
        if (doc == null) return null;
        Timetable tt = new Timetable();
        tt.id = doc.getObjectId("_id");
        tt.timetableId = doc.getString("timetableId");
        tt.classId = doc.getString("classId");
        tt.status = doc.getString("status");
        Long time = doc.getLong("generatedAt");
        tt.generatedAt = time != null ? time : 0L;

        List<Document> slotDocs = (List<Document>) doc.get("slots");
        if (slotDocs != null) {
            for (Document d : slotDocs) {
                tt.slots.add(TimetableSlot.fromDocument(d));
            }
        }
        return tt;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("timetableId", timetableId);
        doc.put("classId", classId);
        doc.put("status", status);
        doc.put("generatedAt", generatedAt);

        List<Document> slotDocs = new ArrayList<>();
        for (TimetableSlot slot : slots) {
            slotDocs.add(slot.toDocument());
        }
        doc.put("slots", slotDocs);
        return doc;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getTimetableId() { return timetableId; }
    public void setTimetableId(String timetableId) { this.timetableId = timetableId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<TimetableSlot> getSlots() { return slots; }
    public void setSlots(List<TimetableSlot> slots) { this.slots = slots; }

    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
}
