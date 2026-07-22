# 🎓 Smart Campus Scheduler (Java CLI + MongoDB)

An intelligent, conflict-free timetable scheduling engine for colleges and schools built with **Pure Java CLI** and **MongoDB** for local storage.

---

## 🌟 Key Features & 5-Module Architecture

### 1. **Module 1: User Authentication & Role Management**
- Secure login system with BCrypt password hashing (`jbcrypt`).
- Role-based access control:
  - **`ADMIN` (HOD/Scheduler)**: Full management, entity setup, algorithm execution, manual swap overrides.
  - **`FACULTY`**: View personal weekly schedule, view assigned periods.

### 2. **Module 2: Academic Setup & Entity Management**
- **Faculty Profiles**: Set max periods/day and max periods/week limits.
- **Subject Catalogue**: Course codes, names, abbreviations, lab flags.
- **Class Requirements**: Specify period demand per class (e.g. `CSE-3A` requires 5 DBMS, 4 Web, 3 OS periods/week).
- **Working Days & Saturday Toggle**: Customize working days (Mon-Fri or Mon-Sat) and daily period counts (5, 6, 7 periods/day).

### 3. **Module 3: Intelligent Scheduling Engine (Core Algorithm)**
- Backtracking Constraint Satisfaction Problem (CSP) Solver.
- Ensures zero faculty double-booking and zero class double-booking.
- Strictly obeys faculty daily & weekly max workload limits.
- Provides actionable diagnostic reports if requirements are mathematically unfeasible.

### 4. **Module 4: CLI Timetable Viewer & Manual Swap Manager**
- Renders formatted ASCII table grids in the terminal.
- Filter schedules by **Class**, **Faculty**, or **Day**.
- Real-time validated manual period swapping.

### 5. **Module 5: Exports & Workload Analytics**
- Export timetables to **`.txt`** (ASCII formatted) and **`.csv`** (Excel / Google Sheets compatible).
- Faculty Workload Utilization Report (% of capacity assigned).

---

## 🚀 Quick Start Guide

### Prerequisites
- Java JDK 17+ installed.
- Local MongoDB service running on `mongodb://localhost:27017`.

### 1. Compile the Project
Double click or run `compile.bat` in your terminal:
```cmd
compile.bat
```

### 2. Run the Application
Run `run.bat` in your terminal:
```cmd
run.bat
```

---

## 🗄️ MongoDB Database Structure
- Database Name: `smart_campus_scheduler`
- Collections: `users`, `faculties`, `subjects`, `classes`, `academic_configs`, `timetables`.