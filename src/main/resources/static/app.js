// Global State
let currentUser = null;
let currentConfig = null;
let allFaculties = [];
let allSubjects = [];
let allClasses = [];
let activeViewMode = 'class'; // 'class' or 'faculty'
let swapModeActive = false;
let swapSelection = null; // { day, period }

// Subject color mapping
const SUBJECT_COLORS = [
    { bg: '#eff6ff', text: '#1e40af', border: '#bfdbfe' },
    { bg: '#f0fdf4', text: '#166534', border: '#bbf7d0' },
    { bg: '#fef3c7', text: '#92400e', border: '#fde68a' },
    { bg: '#f5f3ff', text: '#5b21b6', border: '#ddd6fe' },
    { bg: '#fdf2f8', text: '#9d174d', border: '#fbcfe8' },
    { bg: '#ecfeff', text: '#155e75', border: '#a5f3fc' },
    { bg: '#fff7ed', text: '#9a3412', border: '#ffedd5' }
];

document.addEventListener('DOMContentLoaded', () => {
    checkCurrentUser();
    loadDashboardData();
});

// Toast notification helper
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${type === 'error' ? '❌' : (type === 'success' ? '✅' : 'ℹ️')}</span> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

// Tab Switching
function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));

    const targetTab = document.getElementById(`tab-${tabId}`);
    if (targetTab) {
        targetTab.classList.add('active');
    }

    const clickedBtn = Array.from(document.querySelectorAll('.nav-item')).find(btn => 
        btn.getAttribute('onclick')?.includes(tabId)
    );
    if (clickedBtn) {
        clickedBtn.classList.add('active');
    }

    if (tabId === 'dashboard') loadDashboardData();
    if (tabId === 'timetable') loadTimetableStudio();
    if (tabId === 'faculties') loadFaculties();
    if (tabId === 'subjects') loadSubjects();
    if (tabId === 'classes') loadClasses();
    if (tabId === 'settings') loadAcademicConfig();
    if (tabId === 'exports') loadExportsTab();
}

// Authentication
async function checkCurrentUser() {
    try {
        const res = await fetch('/api/auth/current');
        const data = await res.json();
        if (data.authenticated) {
            currentUser = data.user;
            updateUserUI();
        } else {
            currentUser = { username: 'aakashraj', name: 'Aakash Raj', role: 'ADMIN' };
            updateUserUI();
        }
    } catch (e) {
        currentUser = { username: 'aakashraj', name: 'Aakash Raj', role: 'ADMIN' };
        updateUserUI();
    }
}

function updateUserUI() {
    if (!currentUser) return;
    document.getElementById('userName').innerText = currentUser.name || currentUser.username;
    document.getElementById('userRole').innerText = currentUser.role || 'USER';
    document.getElementById('userAvatar').innerText = (currentUser.name || currentUser.username)[0].toUpperCase();

    // Toggle admin-only sections
    const isAdmin = (currentUser.role || '').toUpperCase() === 'ADMIN';
    document.querySelectorAll('.admin-only').forEach(el => {
        el.style.display = isAdmin ? '' : 'none';
    });
}

function handleAuthAction() {
    openModal('modalLogin');
}

async function submitLoginForm(e) {
    e.preventDefault();
    const username = document.getElementById('loginUser').value;
    const password = document.getElementById('loginPass').value;

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (data.success) {
            currentUser = data.user;
            updateUserUI();
            closeModal('modalLogin');
            showToast(`Welcome back, ${currentUser.name}!`, 'success');
            loadDashboardData();
        } else {
            showToast(data.message || 'Login failed', 'error');
        }
    } catch (err) {
        showToast('Server error during login', 'error');
    }
}

// Modals
function openModal(id) {
    document.getElementById(id)?.classList.remove('hidden');
}

function closeModal(id) {
    document.getElementById(id)?.classList.add('hidden');
}

// Dashboard
async function loadDashboardData() {
    try {
        const [facRes, subRes, clsRes, cfgRes] = await Promise.all([
            fetch('/api/faculties'),
            fetch('/api/subjects'),
            fetch('/api/classes'),
            fetch('/api/config')
        ]);

        allFaculties = await facRes.json();
        allSubjects = await subRes.json();
        allClasses = await clsRes.json();
        currentConfig = await cfgRes.json();

        document.getElementById('statFacultyCount').innerText = allFaculties.length;
        document.getElementById('statSubjectCount').innerText = allSubjects.length;
        document.getElementById('statClassCount').innerText = allClasses.length;
        document.getElementById('statWeeklySlots').innerText = currentConfig.totalSlotsPerClass || 40;
    } catch (e) {
        console.error('Error loading dashboard data', e);
    }
}

// Timetable Studio
async function loadTimetableStudio() {
    await loadDashboardData();

    // Populate Selects
    const clsSelect = document.getElementById('selectClass');
    clsSelect.innerHTML = allClasses.map(c => `<option value="${c.classId}">${c.classId} (${c.department} - Sem ${c.semester})</option>`).join('');

    const facSelect = document.getElementById('selectFaculty');
    facSelect.innerHTML = allFaculties.map(f => `<option value="${f.facultyId}">${f.name} (${f.facultyId})</option>`).join('');

    if (allClasses.length > 0) {
        loadCurrentTimetable();
    }
}

function setTimetableViewMode(mode) {
    activeViewMode = mode;
    document.getElementById('btnModeClass').classList.toggle('active', mode === 'class');
    document.getElementById('btnModeFaculty').classList.toggle('active', mode === 'faculty');

    document.getElementById('filterClassSelectGroup').classList.toggle('hidden', mode !== 'class');
    document.getElementById('filterFacultySelectGroup').classList.toggle('hidden', mode !== 'faculty');

    loadCurrentTimetable();
}

async function loadCurrentTimetable() {
    cancelSwap();
    const container = document.getElementById('timetableGridContainer');
    container.innerHTML = '<div style="padding: 40px; text-align: center; color: #64748b;">Loading timetable grid...</div>';

    if (activeViewMode === 'class') {
        const classId = document.getElementById('selectClass').value;
        if (!classId) {
            container.innerHTML = '<div style="padding: 40px; text-align: center;">No class selected.</div>';
            return;
        }
        document.getElementById('timetableCurrentTitle').innerText = `Class Timetable: ${classId}`;

        try {
            const res = await fetch(`/api/timetable/class/${classId}`);
            if (!res.ok) {
                container.innerHTML = `<div style="padding: 40px; text-align: center; color: #94a3b8;">
                    No timetable generated for Class ${classId} yet.<br>
                    <button class="btn btn-primary btn-sm mt-4" onclick="triggerAutoSchedule()">Generate Now</button>
                </div>`;
                return;
            }
            const data = await res.json();
            renderTimetableGrid(data.slots, data.config, 'class');
        } catch (e) {
            container.innerHTML = '<div style="padding: 40px; text-align: center; color: #ef4444;">Failed to load timetable</div>';
        }
    } else {
        const facultyId = document.getElementById('selectFaculty').value;
        if (!facultyId) return;

        try {
            const res = await fetch(`/api/timetable/faculty/${facultyId}`);
            const data = await res.json();
            document.getElementById('timetableCurrentTitle').innerText = `Faculty Schedule: ${data.facultyName} (${data.facultyId}) - ${data.totalAssigned} Periods/Week`;
            renderTimetableGrid(data.slots, data.config, 'faculty');
        } catch (e) {
            container.innerHTML = '<div style="padding: 40px; text-align: center; color: #ef4444;">Failed to load faculty schedule</div>';
        }
    }
}

function renderTimetableGrid(slots, config, viewType) {
    const container = document.getElementById('timetableGridContainer');
    const workingDays = config.workingDays || ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
    const periodsPerDay = config.periodsPerDay || 8;

    // Create slot lookup map: DAY_PERIOD -> slot
    const slotMap = {};
    if (slots) {
        slots.forEach(s => {
            const key = `${s.day.toUpperCase()}_${s.period}`;
            slotMap[key] = s;
        });
    }

    let html = `<table class="timetable-grid"><thead><tr><th class="day-col">DAY</th>`;
    for (let p = 1; p <= periodsPerDay; p++) {
        html += `<th>Period ${p}</th>`;
    }
    html += `</tr></thead><tbody>`;

    workingDays.forEach(day => {
        html += `<tr><td class="day-col">${day}</td>`;
        for (let p = 1; p <= periodsPerDay; p++) {
            const key = `${day.toUpperCase()}_${p}`;
            const slot = slotMap[key];
            const isClickable = swapModeActive && viewType === 'class';
            const isSelected = swapSelection && swapSelection.day === day && swapSelection.period === p;

            html += `<td class="timetable-slot-cell ${isClickable ? 'clickable' : ''} ${isSelected ? 'selected-swap' : ''}" 
                        onclick="handleSlotClick('${day}', ${p})">`;

            if (slot) {
                const colorIdx = Math.abs(hashCode(slot.subjectShortName || slot.subjectCode)) % SUBJECT_COLORS.length;
                const c = SUBJECT_COLORS[colorIdx];
                const subLabel = slot.subjectShortName || slot.subjectCode;
                const subMeta = viewType === 'class' ? (slot.facultyName || slot.facultyId) : `Class ${slot.classId}`;

                html += `<div class="slot-card" style="background: ${c.bg}; border: 1px solid ${c.border};">
                            <span class="slot-subject" style="color: ${c.text};">${subLabel}</span>
                            <span class="slot-faculty">${subMeta}</span>
                         </div>`;
            } else {
                html += `<span class="slot-empty">---</span>`;
            }
            html += `</td>`;
        }
        html += `</tr>`;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
}

function hashCode(str) {
    let hash = 0;
    if (!str) return hash;
    for (let i = 0; i < str.length; i++) {
        hash = ((hash << 5) - hash) + str.charCodeAt(i);
        hash |= 0;
    }
    return hash;
}

// Auto-scheduler trigger
async function triggerAutoSchedule() {
    showToast('Initializing CSP timetable engine...', 'info');
    try {
        const res = await fetch('/api/timetable/generate', { method: 'POST' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadDashboardData();
            switchTab('timetable');
        } else {
            const errorList = (data.errors || []).join(' | ');
            showToast(`Generation Error: ${data.message} (${errorList})`, 'error');
        }
    } catch (e) {
        showToast('Error connecting to scheduler engine', 'error');
    }
}

// Period Swap Mode
function toggleSwapMode() {
    swapModeActive = !swapModeActive;
    swapSelection = null;
    document.getElementById('btnToggleSwap').classList.toggle('btn-primary', swapModeActive);
    document.getElementById('swapNotice').classList.toggle('hidden', !swapModeActive);
    loadCurrentTimetable();
}

function cancelSwap() {
    swapModeActive = false;
    swapSelection = null;
    document.getElementById('btnToggleSwap')?.classList.remove('btn-primary');
    document.getElementById('swapNotice')?.classList.add('hidden');
}

async function handleSlotClick(day, period) {
    if (!swapModeActive) return;

    if (!swapSelection) {
        // First selection
        swapSelection = { day, period };
        showToast(`Slot 1 selected: ${day} Period ${period}. Now click target slot.`, 'info');
        loadCurrentTimetable();
    } else {
        // Second selection -> execute swap
        const classId = document.getElementById('selectClass').value;
        const body = {
            classId: classId,
            day1: swapSelection.day,
            period1: swapSelection.period,
            day2: day,
            period2: period
        };

        try {
            const res = await fetch('/api/timetable/swap', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Swapped: ${swapSelection.day} P${swapSelection.period} ⟷ ${day} P${period}`, 'success');
                cancelSwap();
                loadCurrentTimetable();
            } else {
                showToast(data.message || 'Swap failed', 'error');
            }
        } catch (e) {
            showToast('Error executing period swap', 'error');
        }
    }
}

// Faculty Management
async function loadFaculties() {
    try {
        const res = await fetch('/api/faculties');
        allFaculties = await res.json();
        const tbody = document.getElementById('facultyTableBody');

        if (allFaculties.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #94a3b8;">No faculties registered yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = allFaculties.map(f => `
            <tr>
                <td><strong>${f.facultyId}</strong></td>
                <td>${f.name}</td>
                <td><span class="badge badge-info">${f.department}</span></td>
                <td>${f.email}</td>
                <td>${f.phone || 'N/A'}</td>
                <td>${f.maxPeriodsPerDay} / day</td>
                <td>${f.maxPeriodsPerWeek} / wk</td>
                <td>
                    <button class="btn-danger-xs" onclick="deleteFaculty('${f.facultyId}')">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Failed to load faculties', 'error');
    }
}

function openFacultyModal() {
    document.getElementById('formFaculty').reset();
    openModal('modalFaculty');
}

async function submitFacultyForm(e) {
    e.preventDefault();
    const payload = {
        username: document.getElementById('facUsername').value,
        password: document.getElementById('facPassword').value,
        facultyId: document.getElementById('facId').value,
        name: document.getElementById('facName').value,
        department: document.getElementById('facDept').value,
        email: document.getElementById('facEmail').value,
        phone: document.getElementById('facPhone').value,
        maxPeriodsPerDay: parseInt(document.getElementById('facMaxDay').value),
        maxPeriodsPerWeek: parseInt(document.getElementById('facMaxWeek').value)
    };

    try {
        const res = await fetch('/api/auth/register-faculty', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Faculty ${payload.name} (${payload.facultyId}) registered!`, 'success');
            closeModal('modalFaculty');
            loadFaculties();
        } else {
            showToast(data.message || 'Registration failed', 'error');
        }
    } catch (err) {
        showToast('Error registering faculty', 'error');
    }
}

async function deleteFaculty(facultyId) {
    if (!confirm(`Are you sure you want to delete Faculty ${facultyId}?`)) return;
    try {
        const res = await fetch(`/api/faculties/${facultyId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadFaculties();
        } else {
            showToast(data.message, 'error');
        }
    } catch (e) {
        showToast('Error deleting faculty', 'error');
    }
}

// Subject Management
async function loadSubjects() {
    try {
        const res = await fetch('/api/subjects');
        allSubjects = await res.json();
        const tbody = document.getElementById('subjectTableBody');

        if (allSubjects.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #94a3b8;">No subjects added yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = allSubjects.map(s => `
            <tr>
                <td><strong>${s.subjectCode}</strong></td>
                <td>${s.name}</td>
                <td><span class="badge badge-warning">${s.shortName}</span></td>
                <td>${s.lab ? '<span class="badge badge-info">LAB / PRACTICAL</span>' : 'Theory'}</td>
                <td>
                    <button class="btn-danger-xs" onclick="deleteSubject('${s.subjectCode}')">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Failed to load subjects', 'error');
    }
}

function openSubjectModal() {
    document.getElementById('formSubject').reset();
    openModal('modalSubject');
}

async function submitSubjectForm(e) {
    e.preventDefault();
    const payload = {
        subjectCode: document.getElementById('subCode').value,
        name: document.getElementById('subName').value,
        shortName: document.getElementById('subShort').value,
        isLab: document.getElementById('subIsLab').checked
    };

    try {
        const res = await fetch('/api/subjects', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Subject ${payload.shortName} saved!`, 'success');
            closeModal('modalSubject');
            loadSubjects();
        } else {
            showToast(data.message || 'Error saving subject', 'error');
        }
    } catch (e) {
        showToast('Server error saving subject', 'error');
    }
}

async function deleteSubject(code) {
    if (!confirm(`Delete subject ${code}?`)) return;
    try {
        const res = await fetch(`/api/subjects/${code}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadSubjects();
        } else {
            showToast(data.message, 'error');
        }
    } catch (e) {
        showToast('Error deleting subject', 'error');
    }
}

// Class Requirements
async function loadClasses() {
    try {
        const [clsRes, facRes, subRes] = await Promise.all([
            fetch('/api/classes'),
            fetch('/api/faculties'),
            fetch('/api/subjects')
        ]);
        allClasses = await clsRes.json();
        allFaculties = await facRes.json();
        allSubjects = await subRes.json();

        const container = document.getElementById('classesListContainer');
        if (allClasses.length === 0) {
            container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">
                No classes configured. Click "Add Class Section" to begin.
            </div>`;
            return;
        }

        const subNameMap = {};
        allSubjects.forEach(s => subNameMap[s.subjectCode] = s.shortName || s.name);

        const facNameMap = {};
        allFaculties.forEach(f => facNameMap[f.facultyId] = f.name);

        container.innerHTML = allClasses.map(cg => {
            let totalPeriods = 0;
            const demandsHtml = (cg.subjectDemands || []).map(d => {
                totalPeriods += (d.periodsPerWeek || 0);
                const sName = subNameMap[d.subjectCode] || d.subjectCode;
                const fName = facNameMap[d.facultyId] || d.facultyId;
                return `<li class="class-demand-item">
                            <span><strong>${sName}</strong> (${d.subjectCode}) &bull; ${fName}</span>
                            <span class="badge badge-info">${d.periodsPerWeek} periods/wk</span>
                        </li>`;
            }).join('');

            return `
                <div class="class-card">
                    <div class="class-card-header">
                        <div>
                            <div class="class-title">${cg.classId}</div>
                            <span class="text-muted" style="font-size: 12.5px;">Dept: ${cg.department} &bull; Semester ${cg.semester}</span>
                        </div>
                        <button class="btn-danger-xs" onclick="deleteClassGroup('${cg.classId}')">Delete</button>
                    </div>
                    <ul class="class-demands-list">
                        ${demandsHtml || '<li class="text-muted" style="font-size: 12px;">No subjects assigned yet.</li>'}
                    </ul>
                    <div class="between" style="font-size: 13px; font-weight: 600;">
                        <span>Total Weekly Demand:</span>
                        <span class="badge ${totalPeriods === 40 ? 'badge-success' : 'badge-warning'}">${totalPeriods} Slots</span>
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        showToast('Error loading class requirements', 'error');
    }
}

function openClassModal() {
    document.getElementById('formClass').reset();
    document.getElementById('classDemandsList').innerHTML = '';
    addClassDemandRow();
    openModal('modalClass');
}

function addClassDemandRow(selectedSub = '', selectedFac = '', periods = 8) {
    const container = document.getElementById('classDemandsList');
    const div = document.createElement('div');
    div.className = 'demand-row';

    const subOptions = allSubjects.map(s => 
        `<option value="${s.subjectCode}" ${s.subjectCode === selectedSub ? 'selected' : ''}>${s.shortName || s.subjectCode} - ${s.name}</option>`
    ).join('');

    const facOptions = allFaculties.map(f => 
        `<option value="${f.facultyId}" ${f.facultyId === selectedFac ? 'selected' : ''}>${f.name} (${f.facultyId})</option>`
    ).join('');

    div.innerHTML = `
        <select class="form-select demand-subject" required>
            <option value="">-- Choose Subject --</option>
            ${subOptions}
        </select>
        <select class="form-select demand-faculty" required>
            <option value="">-- Assign Faculty --</option>
            ${facOptions}
        </select>
        <input type="number" class="form-input demand-periods" value="${periods}" min="1" max="25" placeholder="Periods" required>
        <button type="button" class="btn-danger-xs" onclick="this.parentElement.remove()">✕</button>
    `;
    container.appendChild(div);
}

async function submitClassForm(e) {
    e.preventDefault();
    const classId = document.getElementById('clsId').value;
    const department = document.getElementById('clsDept').value;
    const semester = parseInt(document.getElementById('clsSem').value);

    const rows = document.querySelectorAll('#classDemandsList .demand-row');
    const demands = [];
    rows.forEach(r => {
        const sub = r.querySelector('.demand-subject').value;
        const fac = r.querySelector('.demand-faculty').value;
        const p = parseInt(r.querySelector('.demand-periods').value);
        if (sub && fac && p > 0) {
            demands.push({ subjectCode: sub, facultyId: fac, periodsPerWeek: p });
        }
    });

    const payload = {
        classId,
        department,
        semester,
        subjectDemands: demands
    };

    try {
        const res = await fetch('/api/classes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Class ${classId} requirements saved!`, 'success');
            closeModal('modalClass');
            loadClasses();
        } else {
            showToast(data.message || 'Error saving class', 'error');
        }
    } catch (e) {
        showToast('Server error saving class', 'error');
    }
}

async function deleteClassGroup(classId) {
    if (!confirm(`Delete class ${classId}?`)) return;
    try {
        const res = await fetch(`/api/classes/${classId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadClasses();
        } else {
            showToast(data.message, 'error');
        }
    } catch (e) {
        showToast('Error deleting class', 'error');
    }
}

// Academic Settings
async function loadAcademicConfig() {
    try {
        const res = await fetch('/api/config');
        currentConfig = await res.json();
        document.getElementById('cfgIncludeSaturday').checked = currentConfig.includeSaturday;
        document.getElementById('cfgPeriodsPerDay').value = currentConfig.periodsPerDay || 8;
        updateConfigCapacity();
    } catch (e) {
        showToast('Error loading configuration', 'error');
    }
}

function updateConfigCapacity() {
    const isSat = document.getElementById('cfgIncludeSaturday').checked;
    const periods = parseInt(document.getElementById('cfgPeriodsPerDay').value) || 8;
    const days = isSat ? 6 : 5;
    document.getElementById('cfgTotalCapacity').innerText = days * periods;
}

document.getElementById('cfgIncludeSaturday')?.addEventListener('change', updateConfigCapacity);
document.getElementById('cfgPeriodsPerDay')?.addEventListener('input', updateConfigCapacity);

async function saveAcademicConfig(e) {
    e.preventDefault();
    const includeSaturday = document.getElementById('cfgIncludeSaturday').checked;
    const periodsPerDay = parseInt(document.getElementById('cfgPeriodsPerDay').value);

    try {
        const res = await fetch('/api/config', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ includeSaturday, periodsPerDay })
        });
        const data = await res.json();
        if (data.success) {
            currentConfig = data.config;
            showToast('Academic configuration updated!', 'success');
        } else {
            showToast(data.message || 'Error updating config', 'error');
        }
    } catch (e) {
        showToast('Error saving configuration', 'error');
    }
}

// Exports & Analytics
async function loadExportsTab() {
    await loadDashboardData();

    // Populate dropdowns
    const txtSelect = document.getElementById('exportTxtClassSelect');
    const csvSelect = document.getElementById('exportCsvClassSelect');

    const options = allClasses.map(c => `<option value="${c.classId}">${c.classId}</option>`).join('');
    txtSelect.innerHTML = options;
    csvSelect.innerHTML = options;

    // Load Analytics
    try {
        const res = await fetch('/api/timetable/analytics');
        const data = await res.json();
        const tbody = document.getElementById('analyticsTableBody');

        if (!data.reports || data.reports.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #94a3b8;">No workload reports available yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.reports.map(r => `
            <tr>
                <td><strong>${r.facultyId}</strong></td>
                <td>${r.name}</td>
                <td><span class="badge badge-info">${r.department}</span></td>
                <td>${r.assigned} periods</td>
                <td>${r.maxWeekly} periods</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <div style="flex: 1; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden;">
                            <div style="height: 100%; width: ${Math.min(r.utilizationRate, 100)}%; background: ${r.utilizationRate > 90 ? '#ef4444' : '#10b981'};"></div>
                        </div>
                        <span style="font-weight: 600; font-size: 12px;">${r.utilizationRate}%</span>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading analytics', 'error');
    }
}

function downloadTxtExport() {
    const classId = document.getElementById('exportTxtClassSelect').value;
    if (!classId) return;
    window.open(`/api/timetable/export/txt/${classId}`, '_blank');
}

function downloadCsvExport() {
    const classId = document.getElementById('exportCsvClassSelect').value;
    if (!classId) return;
    window.open(`/api/timetable/export/csv/${classId}`, '_blank');
}
