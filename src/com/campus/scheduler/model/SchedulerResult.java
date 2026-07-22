package com.campus.scheduler.model;

import java.util.*;

public class SchedulerResult {
    private boolean success;
    private List<String> errorMessages = new ArrayList<>();
    private Map<String, List<TimetableSlot>> classSchedules = new HashMap<>();

    public SchedulerResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<String> getErrorMessages() { return errorMessages; }
    public void addError(String error) { this.errorMessages.add(error); }

    public Map<String, List<TimetableSlot>> getClassSchedules() { return classSchedules; }
    public void setClassSchedules(Map<String, List<TimetableSlot>> classSchedules) {
        this.classSchedules = classSchedules;
    }
}
