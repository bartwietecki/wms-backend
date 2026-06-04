package com.workforce.wms.api.employee;

import com.workforce.wms.AbstractIntegrationTest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.MonthlyWorkReport;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.MonthlyWorkReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MonthlyReportPdfIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MonthlyWorkReportRepository reportRepository;

    // Unauthenticated → 401
    @Test
    void exportPdf_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/reports/1/pdf"))
                .andExpect(status().isUnauthorized());
    }

    // Employee exports own report → 200 PDF
    @Test
    void exportPdf_ownReport_shouldReturnValidPdf() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        MonthlyWorkReport report = persistReport(jan, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 960);

        MvcResult result = mockMvc.perform(get("/api/reports/" + report.getId() + "/pdf")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"monthly-report-" + jan.getId() + "-2026-3.pdf\""))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).hasSizeGreaterThan(4);
        assertThat(new String(body, 0, 4)).isEqualTo("%PDF");
    }

    // Employee exports another employee's report → 403
    @Test
    void exportPdf_anotherEmployeesReport_shouldReturn403() throws Exception {
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();
        MonthlyWorkReport report = persistReport(anna, 2026, 3, MonthlyWorkReportStatus.SUBMITTED, 480);

        mockMvc.perform(get("/api/reports/" + report.getId() + "/pdf")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isForbidden());
    }

    // Report not found → 404
    @Test
    void exportPdf_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/reports/99999/pdf")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isNotFound());
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
