package com.campus.scheduler.engine;

import com.campus.scheduler.model.*;
import java.util.*;

public class TimetableGenerator {

    private final AcademicConfig config;
    private final List<ClassGroup> classes;
    private final Map<String, Faculty> facultyMap = new HashMap<>();

    public TimetableGenerator(AcademicConfig config, List<ClassGroup> classes, List<Faculty> faculties) {
        this.config = config;
        this.classes = classes;
        for (Faculty f : faculties) {
            facultyMap.put(f.getFacultyId(), f);
        }
    }

    public SchedulerResult generate() {
        SchedulerResult result = new SchedulerResult(false);

        if (classes.isEmpty()) {
            result.addError("No classes configured. Please add class subject requirements first.");
            return result;
        }

        int totalSlotsPerWeek = config.getTotalSlotsPerClass();

        // 1. Pre-validation checks
        for (ClassGroup cg : classes) {
            int totalDemand = 0;
            for (SubjectDemand sd : cg.getSubjectDemands()) {
                totalDemand += sd.getPeriodsPerWeek();

                Faculty f = facultyMap.get(sd.getFacultyId());
                if (f == null) {
                    result.addError("Class " + cg.getClassId() + " requires faculty " + sd.getFacultyId() + " which does not exist!");
                    return result;
                }
            }

            if (totalDemand > totalSlotsPerWeek) {
                result.addError("Class " + cg.getClassId() + " total requested periods (" + totalDemand + 
                                ") exceeds available weekly slots (" + totalSlotsPerWeek + "). " +
                                "Consider enabling Saturday or reducing period demands.");
                return result;
            }
        }

        // Faculty total weekly demand check
        Map<String, Integer> facultyWeeklyDemand = new HashMap<>();
        for (ClassGroup cg : classes) {
            for (SubjectDemand sd : cg.getSubjectDemands()) {
                facultyWeeklyDemand.put(
                    sd.getFacultyId(),
                    facultyWeeklyDemand.getOrDefault(sd.getFacultyId(), 0) + sd.getPeriodsPerWeek()
                );
            }
        }

        for (Map.Entry<String, Integer> entry : facultyWeeklyDemand.entrySet()) {
            Faculty f = facultyMap.get(entry.getKey());
            if (f != null && entry.getValue() > f.getMaxPeriodsPerWeek()) {
                result.addError("Faculty " + f.getName() + " (" + f.getFacultyId() + ") total assigned periods (" +
                                entry.getValue() + ") exceeds their max weekly workload limit (" + f.getMaxPeriodsPerWeek() + ").");
                return result;
            }
        }

        // 2. Prepare schedules structure
        Map<String, List<TimetableSlot>> classSchedules = new HashMap<>();
        for (ClassGroup cg : classes) {
            classSchedules.put(cg.getClassId(), new ArrayList<>());
        }

        // Track faculty daily & weekly counts
        Map<String, Map<String, Integer>> facultyDailyCount = new HashMap<>();
        Map<String, Integer> facultyWeeklyCount = new HashMap<>();

        // 3. Solve using Backtracking search class by class
        boolean solved = solveClassRecursively(0, classes, classSchedules, facultyDailyCount, facultyWeeklyCount);

        if (solved) {
            result.setSuccess(true);
            result.setClassSchedules(classSchedules);
        } else {
            result.addError("Could not find a conflict-free timetable. Try increasing faculty max daily limits or expanding working days.");
        }

        return result;
    }

    private boolean solveClassRecursively(
            int classIndex,
            List<ClassGroup> classesList,
            Map<String, List<TimetableSlot>> allSchedules,
            Map<String, Map<String, Integer>> facultyDailyCount,
            Map<String, Integer> facultyWeeklyCount
    ) {
        if (classIndex >= classesList.size()) {
            return true;
        }

        ClassGroup currentClass = classesList.get(classIndex);
        List<SubjectDemandItem> demandItems = expandAndInterleaveDemands(currentClass.getSubjectDemands());

        return solveDemandsForClass(0, demandItems, currentClass, allSchedules, facultyDailyCount, facultyWeeklyCount, classIndex, classesList);
    }

    private boolean solveDemandsForClass(
            int itemIndex,
            List<SubjectDemandItem> items,
            ClassGroup targetClass,
            Map<String, List<TimetableSlot>> allSchedules,
            Map<String, Map<String, Integer>> facultyDailyCount,
            Map<String, Integer> facultyWeeklyCount,
            int classIndex,
            List<ClassGroup> classesList
    ) {
        if (itemIndex >= items.size()) {
            return solveClassRecursively(classIndex + 1, classesList, allSchedules, facultyDailyCount, facultyWeeklyCount);
        }

        SubjectDemandItem item = items.get(itemIndex);
        Faculty faculty = facultyMap.get(item.facultyId);
        List<TimetableSlot> currentClassSlots = allSchedules.get(targetClass.getClassId());

        List<String> days = config.getWorkingDays();
        int periodsPerDay = config.getPeriodsPerDay();

        for (String day : days) {
            for (int p = 1; p <= periodsPerDay; p++) {
                if (isClassSlotOccupied(currentClassSlots, day, p)) {
                    continue;
                }

                int currentDayFacCount = facultyDailyCount
                        .getOrDefault(item.facultyId, new HashMap<>())
                        .getOrDefault(day, 0);
                int currentWeekFacCount = facultyWeeklyCount
                        .getOrDefault(item.facultyId, 0);

                int currentDaySubjCount = getSubjectCountForDay(currentClassSlots, day, item.subjectCode);

                if (ConstraintChecker.isValidAssignment(
                        day, p, targetClass,
                        new SubjectDemand(item.subjectCode, item.facultyId, 1),
                        faculty, allSchedules,
                        currentDaySubjCount, currentDayFacCount, currentWeekFacCount
                )) {
                    TimetableSlot slot = new TimetableSlot(day, p, targetClass.getClassId(), item.subjectCode, item.facultyId);
                    currentClassSlots.add(slot);

                    facultyDailyCount.computeIfAbsent(item.facultyId, k -> new HashMap<>())
                            .put(day, currentDayFacCount + 1);
                    facultyWeeklyCount.put(item.facultyId, currentWeekFacCount + 1);

                    if (solveDemandsForClass(itemIndex + 1, items, targetClass, allSchedules, facultyDailyCount, facultyWeeklyCount, classIndex, classesList)) {
                        return true;
                    }

                    // Backtrack
                    currentClassSlots.remove(slot);
                    facultyDailyCount.get(item.facultyId).put(day, currentDayFacCount);
                    facultyWeeklyCount.put(item.facultyId, currentWeekFacCount);
                }
            }
        }

        return false;
    }

    private boolean isClassSlotOccupied(List<TimetableSlot> slots, String day, int period) {
        for (TimetableSlot s : slots) {
            if (s.getDay().equalsIgnoreCase(day) && s.getPeriod() == period) {
                return true;
            }
        }
        return false;
    }

    private int getSubjectCountForDay(List<TimetableSlot> slots, String day, String subjectCode) {
        int count = 0;
        for (TimetableSlot s : slots) {
            if (s.getDay().equalsIgnoreCase(day) && s.getSubjectCode().equalsIgnoreCase(subjectCode)) {
                count++;
            }
        }
        return count;
    }

    private List<SubjectDemandItem> expandAndInterleaveDemands(List<SubjectDemand> demands) {
        List<SubjectDemandItem> list = new ArrayList<>();
        if (demands.isEmpty()) return list;

        int maxPeriods = 0;
        for (SubjectDemand sd : demands) {
            maxPeriods = Math.max(maxPeriods, sd.getPeriodsPerWeek());
        }

        // Interleave subject demands round-robin style (OS, JAVA, DAA, CAO, ML, OS, JAVA, DAA...)
        for (int i = 0; i < maxPeriods; i++) {
            for (SubjectDemand sd : demands) {
                if (i < sd.getPeriodsPerWeek()) {
                    list.add(new SubjectDemandItem(sd.getSubjectCode(), sd.getFacultyId()));
                }
            }
        }
        return list;
    }

    private static class SubjectDemandItem {
        String subjectCode;
        String facultyId;

        SubjectDemandItem(String subjectCode, String facultyId) {
            this.subjectCode = subjectCode;
            this.facultyId = facultyId;
        }
    }
}
