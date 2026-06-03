package com.workforce.wms.api.admin;

import com.workforce.wms.AbstractIntegrationTest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.MonthlyWorkReport;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.MonthlyWorkReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminMonthlyReportIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MonthlyWorkReportRepository reportRepository;

    // Admin lists all reports → 200 with content
    @Test
    void getAll_whenAdmin_shouldReturnAllReports() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();
        persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);
        persistReport(anna, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 480);

        mockMvc.perform(get("/api/admin/reports")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // Filter by status=SUBMITTED → returns only SUBMITTED
    @Test
    void getAll_whenFilteredByStatus_shouldReturnOnlyMatching() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();
        persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);
        persistReport(anna, 2026, 3, MonthlyWorkReportStatus.APPROVED, 480);

        mockMvc.perform(get("/api/admin/reports")
                        .param("status", "SUBMITTED")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("SUBMITTED"));
    }

    // Get report detail includes entries for that employee/month
    @Test
    void getById_shouldReturnDetailWithEntries() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        // Jan has 2 work entries in March 2026 from fixture
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);

        mockMvc.perform(get("/api/admin/reports/" + report.getId())
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.entries", hasSize(2)));
    }

    // Not found → 404
    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/admin/reports/99999")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isNotFound());
    }

    // Approve SUBMITTED → APPROVED, reviewedAt set
    @Test
    void approve_whenSubmitted_shouldReturnApproved() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);

        mockMvc.perform(post("/api/admin/reports/" + report.getId() + "/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.id").value(report.getId()))
                .andExpect(jsonPath("$.reviewedAt").isNotEmpty());
    }

    // Reject SUBMITTED with comment → REJECTED
    @Test
    void reject_whenSubmitted_shouldReturnRejected() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);

        mockMvc.perform(post("/api/admin/reports/" + report.getId() + "/reject")
                        .with(httpBasic(ADMIN, ADMIN_PASS))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "adminComment": "Missing project codes" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.adminComment").value("Missing project codes"))
                .andExpect(jsonPath("$.reviewedAt").isNotEmpty());
    }

    // Reject without comment → 400 (validation)
    @Test
    void reject_withoutComment_shouldReturn400() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);

        mockMvc.perform(post("/api/admin/reports/" + report.getId() + "/reject")
                        .with(httpBasic(ADMIN, ADMIN_PASS))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "adminComment": "" }
                                """))
                .andExpect(status().isBadRequest());
    }

    // Approve non-SUBMITTED → 400
    @Test
    void approve_whenAlreadyApproved_shouldReturn400() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.APPROVED, 960);

        mockMvc.perform(post("/api/admin/reports/" + report.getId() + "/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isBadRequest());
    }

    // Reject non-SUBMITTED → 400
    @Test
    void reject_whenAlreadyRejected_shouldReturn400() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.REJECTED, 960);

        mockMvc.perform(post("/api/admin/reports/" + report.getId() + "/reject")
                        .with(httpBasic(ADMIN, ADMIN_PASS))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "adminComment": "Still wrong" }
                                """))
                .andExpect(status().isBadRequest());
    }

    // Approve not found → 404
    @Test
    void approve_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/admin/reports/99999/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isNotFound());
    }

    // Admin cannot use employee submit endpoint → 403
    @Test
    void submit_whenCalledByAdmin_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/submit")
                        .param("year", "2026").param("month", "3")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
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
