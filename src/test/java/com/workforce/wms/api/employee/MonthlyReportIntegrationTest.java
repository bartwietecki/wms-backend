package com.workforce.wms.api.employee;

import com.workforce.wms.AbstractIntegrationTest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.MonthlyWorkReport;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.MonthlyWorkReportRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MonthlyReportIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MonthlyWorkReportRepository reportRepository;

    @Autowired
    private WorkEntryRepository workEntryRepository;

    // Unauthenticated → 401
    @Test
    void preview_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/preview")
                        .param("year", "2026").param("month", "3"))
                .andExpect(status().isUnauthorized());
    }

    // Preview returns calculated totals and does NOT persist a report
    @Test
    void preview_shouldReturnCalculatedTotalsWithoutPersisting() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();

        // Jan has 2 entries in March 2026 (480 min each) from fixture
        mockMvc.perform(get("/api/reports/monthly/preview")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.totalMinutes").value(960))
                .andExpect(jsonPath("$.totalHours").value(16))
                .andExpect(jsonPath("$.entriesCount").value(2))
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.existingReportStatus").isEmpty());

        assertThat(reportRepository.findByEmployeeIdAndYearAndMonth(jan.getId(), 2026, 3)).isEmpty();
    }

    // Preview when report already submitted shows existing status
    @Test
    void preview_whenReportAlreadyExists_shouldShowExistingStatus() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);

        mockMvc.perform(get("/api/reports/monthly/preview")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existingReportStatus").value("SUBMITTED"));
    }

    // Submit creates SUBMITTED report with correct totalMinutes
    @Test
    void submit_shouldCreateSubmittedReportWithCalculatedTotal() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/submit")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.totalMinutes").value(960))
                .andExpect(jsonPath("$.totalHours").value(16))
                .andExpect(jsonPath("$.employeeName").value("Jan Kowalski"));
    }

    // Submit for month with no entries → 400
    @Test
    void submit_whenNoEntries_shouldReturn400() throws Exception {
        // June 2026 has no fixture entries
        mockMvc.perform(post("/api/reports/monthly/submit")
                        .param("year", "2026").param("month", "6")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isBadRequest());
    }

    // Submit same month twice when already SUBMITTED → 400
    @Test
    void submit_whenAlreadySubmitted_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/submit")
                .param("year", "2026").param("month", "3")
                .with(httpBasic(JAN, EMP_PASS)));

        mockMvc.perform(post("/api/reports/monthly/submit")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isBadRequest());
    }

    // Submit when report is REJECTED → allowed (resubmit), recalculates total
    @Test
    void submit_whenRejected_shouldAllowResubmitAndRecalculateTotal() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        persistReport(jan, 2026, 3, MonthlyWorkReportStatus.REJECTED, 100);

        mockMvc.perform(post("/api/reports/monthly/submit")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.totalMinutes").value(960));  // recalculated from entries
    }

    // Submit when report is APPROVED → 400
    @Test
    void submit_whenAlreadyApproved_shouldReturn400() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        persistReport(jan, 2026, 3, MonthlyWorkReportStatus.APPROVED, 960);

        mockMvc.perform(post("/api/reports/monthly/submit")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isBadRequest());
    }

    // My reports returns only authenticated employee's reports, sorted newest first
    @Test
    void myReports_shouldReturnOnlyOwnReports() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();

        persistReport(jan, 2026, 2, MonthlyWorkReportStatus.APPROVED, 800);
        persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);
        persistReport(anna, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 480);

        mockMvc.perform(get("/api/reports/my")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].month").value(3))   // newest first
                .andExpect(jsonPath("$[1].month").value(2));
    }

    // Preview entries are sorted by workDate ASC (chronological order)
    @Test
    void preview_shouldReturnEntriesSortedByWorkDateAsc() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();

        // Fixture gives Jan 2 entries on 2026-03-01. Add one on 2026-03-05 (later date).
        WorkEntry laterEntry = new WorkEntry();
        laterEntry.setEmployee(jan);
        laterEntry.setWorkDate(LocalDate.of(2026, 3, 5));
        laterEntry.setMinutes(120);
        laterEntry.setDescription("Later entry");
        laterEntry.setStatus(WorkEntryStatus.PENDING);
        workEntryRepository.save(laterEntry);

        mockMvc.perform(get("/api/reports/monthly/preview")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(3)))
                .andExpect(jsonPath("$.entries[0].workDate").value("2026-03-01"))  // earliest first
                .andExpect(jsonPath("$.entries[2].workDate").value("2026-03-05")); // latest last
    }

    // Employee cannot access admin reports endpoint → 403
    @Test
    void adminReports_whenCalledByEmployee_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/reports")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isForbidden());
    }

    private MonthlyWorkReport persistReport(Employee employee, int year, int month,
                                            MonthlyWorkReportStatus status, int totalMinutes) {
        MonthlyWorkReport report = new MonthlyWorkReport();
        report.setEmployee(employee);
        report.setYear(year);
        report.setMonth(month);
        report.setStatus(status);
        report.setTotalMinutes(totalMinutes);
        return reportRepository.save(report);
    }
}
