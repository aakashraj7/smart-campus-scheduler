package com.campus.scheduler.model;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AcademicConfig {
    private ObjectId id;
    private String configName;
    private boolean includeSaturday;
    private int periodsPerDay;
    private List<String> workingDays = new ArrayList<>();

    public AcademicConfig() {
        // Defaults: Mon-Fri, 6 periods/day
        this.configName = "DEFAULT_CONFIG";
        this.includeSaturday = false;
        this.periodsPerDay = 6;
        this.workingDays = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
    }

    public AcademicConfig(String configName, boolean includeSaturday, int periodsPerDay) {
        this.configName = configName;
        this.includeSaturday = includeSaturday;
        this.periodsPerDay = periodsPerDay;
        rebuildWorkingDays();
    }

    public void rebuildWorkingDays() {
        if (includeSaturday) {
            this.workingDays = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");
        } else {
            this.workingDays = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
        }
    }

    @SuppressWarnings("unchecked")
    public static AcademicConfig fromDocument(Document doc) {
        if (doc == null) return new AcademicConfig();
        AcademicConfig config = new AcademicConfig();
        config.id = doc.getObjectId("_id");
        config.configName = doc.getString("configName");
        Boolean sat = doc.getBoolean("includeSaturday");
        config.includeSaturday = sat != null ? sat : false;
        Integer periods = doc.getInteger("periodsPerDay");
        config.periodsPerDay = periods != null ? periods : 6;
        List<String> days = (List<String>) doc.get("workingDays");
        if (days != null && !days.isEmpty()) {
            config.workingDays = days;
        } else {
            config.rebuildWorkingDays();
        }
        return config;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.put("_id", id);
        doc.put("configName", configName);
        doc.put("includeSaturday", includeSaturday);
        doc.put("periodsPerDay", periodsPerDay);
        doc.put("workingDays", workingDays);
        return doc;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public boolean isIncludeSaturday() { return includeSaturday; }
    public void setIncludeSaturday(boolean includeSaturday) {
        this.includeSaturday = includeSaturday;
        rebuildWorkingDays();
    }

    public int getPeriodsPerDay() { return periodsPerDay; }
    public void setPeriodsPerDay(int periodsPerDay) { this.periodsPerDay = periodsPerDay; }

    public List<String> getWorkingDays() { return workingDays; }
    public void setWorkingDays(List<String> workingDays) { this.workingDays = workingDays; }

    public int getTotalSlotsPerClass() {
        return workingDays.size() * periodsPerDay;
    }
}
