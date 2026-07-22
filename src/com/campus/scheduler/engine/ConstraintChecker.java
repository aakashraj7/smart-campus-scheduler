package com.campus.scheduler.engine;

import com.campus.scheduler.model.*;
import java.util.*;

public class ConstraintChecker {

    /**
     * Check if placing subject and faculty into (day, period) for targetClass violates any hard or soft constraint.
     */
    public static boolean isValidAssignment(
            String day,
            int period,
            ClassGroup targetClass,
            SubjectDemand demand,
            Faculty faculty,
            Map<String, List<TimetableSlot>> allClassSchedules,
            int currentDaySubjectCount,
            int currentDayFacultyCount,
            int currentWeekFacultyCount
    ) {
        // 1. Faculty max periods per day check
        if (currentDayFacultyCount >= faculty.getMaxPeriodsPerDay()) {
            return false;
        }

        // 2. Faculty max periods per week check
        if (currentWeekFacultyCount >= faculty.getMaxPeriodsPerWeek()) {
            return false;
        }

        // 3. Subject daily max limit (Soft constraint: max 2 periods of same subject per day per class)
        if (currentDaySubjectCount >= 2) {
            return false;
        }

        // 4. Faculty Double-Booking Check across ALL classes scheduled so far
        for (Map.Entry<String, List<TimetableSlot>> entry : allClassSchedules.entrySet()) {
            List<TimetableSlot> slots = entry.getValue();
            for (TimetableSlot slot : slots) {
                if (slot.getDay().equalsIgnoreCase(day) && slot.getPeriod() == period) {
                    // Check if faculty is already busy elsewhere at this exact slot
                    if (slot.getFacultyId().equalsIgnoreCase(demand.getFacultyId())) {
                        return false; // Collision!
                    }
                }
            }
        }

        return true;
    }
}
