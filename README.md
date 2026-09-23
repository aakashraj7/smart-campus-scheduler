# 🎓 Smart Campus Scheduler (Java Spring Boot + MongoDB)

An intelligent, conflict-free timetable scheduling engine and modern web application for colleges and schools built with **Java Spring Boot**, a responsive **Single-Page Application (SPA)** dashboard, and **MongoDB** for local storage.

---

## 🌟 Key Features & 5-Module Architecture

### 1. **Module 1: User Authentication & Role Management**
- Secure login system with BCrypt password hashing (`jbcrypt`).
- Role-based access control:
  - **`ADMIN` (HOD/Scheduler)**: Full dashboard management, entity setup, algorithm execution, manual swap overrides.
  - **`FACULTY`**: View personal weekly schedule, view assigned periods, and register new faculty accounts.

### 2. **Module 2: Academic Setup & Entity Management**
- **Faculty Profiles**: Live mobile (`^[6-9]\d{9}$`) and email validation, max periods/day and max periods/week limits.
- **Subject Catalogue**: Course codes, names, short display names (e.g., `OS`, `JAVA`, `DAA`), and lab flags.
- **Class Requirements**: Specify period demand per class (e.g. `CSE-3A` requires 5 DBMS, 4 Web, 3 OS periods/week) with dynamic faculty assignments.
- **Working Days & Saturday Toggle**: Customize working days (Mon-Fri or Mon-Sat) and daily period counts (5, 6, 7, 8 periods/day).

### 3. **Module 3: Intelligent Scheduling Engine (Core CSP Algorithm)**
- Backtracking Constraint Satisfaction Problem (CSP) Solver.
- Ensures zero faculty double-booking and zero class double-booking.
- Strictly obeys faculty daily & weekly max workload limits.
- Generates 100% full, conflict-free schedules with clear diagnostic feedback.

### 4. **Module 4: Web Timetable Studio & Interactive Manual Swap**
- Interactive timetable grid with clean cards, color coding, and readable **subject short names**.
- Filter schedules dynamically by **Class** or **Faculty**.
- Two-click interactive period swapping with real-time constraint validation.
- CLI fallback mode (`--cli`) still fully supported.

### 5. **Module 5: Exports & Workload Analytics**
- Direct browser downloads for **`.csv`** (Excel / Google Sheets compatible) and **`.txt`** (ASCII formatted) timetables.
- Visual faculty workload utilization bar chart with period counts and percentage capacities.

---

## 🚀 Quick Start Guide

### Prerequisites
- Java JDK 17+ installed.
- Local MongoDB service running on `mongodb://localhost:27017`.

### 1. Run the Web Application
Execute `run.ps1` in PowerShell or `run.bat` in CMD:
```powershell
.\run.ps1
```
Open your browser and navigate to:
```
http://localhost:8080
```
Default Administrator credentials:
- **Username**: `admin`
- **Password**: `admin123`

### 2. Compile the Project
```powershell
.\compile.ps1
```

---

## 🌐 REST API Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register-faculty` - Register faculty profile & credentials
- `GET /api/faculties` | `POST /api/faculties` | `DELETE /api/faculties/{id}` - Faculty management
- `GET /api/subjects` | `POST /api/subjects` | `DELETE /api/subjects/{id}` - Subject catalog
- `GET /api/classes` | `POST /api/classes` | `DELETE /api/classes/{id}` - Class requirements
- `GET /api/config` | `PUT /api/config` - Academic rules and configuration
- `POST /api/timetable/generate` - Run CSP timetable generation engine
- `GET /api/timetable/class/{id}` - Class timetable with short names
- `GET /api/timetable/faculty/{id}` - Faculty personalized timetable
- `POST /api/timetable/swap` - Conflict-checked period slot swapping
- `GET /api/timetable/export/csv/{id}` - Download CSV timetable
- `GET /api/timetable/export/txt/{id}` - Download TXT timetable
- `GET /api/timetable/analytics` - Workload utilization metrics

---

## 🗄️ MongoDB Database Structure
- Database Name: `smart_campus_scheduler`
- Collections: `users`, `faculties`, `subjects`, `classes`, `academic_configs`, `timetables`.