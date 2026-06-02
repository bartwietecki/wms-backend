package com.workforce.wms.api.admin;

import com.workforce.wms.AbstractIntegrationTest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.LeaveRequest;
import com.workforce.wms.entity.LeaveRequestStatus;
import com.workforce.wms.entity.LeaveType;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminDashboardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Unauthenticated → 401
    @Test
    void getDashboard_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // Employee role → 403
    @Test
    void getDashboard_whenCalledByEmployee_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isForbidden());
    }

    // Admin → 200
    @Test
    void getDashboard_whenCalledByAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk());
    }

    // Fixture: Jan (PENDING + APPROVED) + Anna (PENDING) → 2 PENDING total
    @Test
    void getDashboard_pendingApprovalsCount_shouldCountAllPendingEntries() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingApprovalsCount").value(2));
    }

    // Fixture: Jan + Anna both active → 2 active employees
    @Test
    void getDashboard_activeEmployeesCount_shouldCountAllActiveEmployees() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeEmployeesCount").value(2));
    }

    // No approved leaves → 0
    @Test
    void getDashboard_whenNoApprovedLeaveCoversToday_shouldReturnZero() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeesOnLeaveToday").value(0));
    }

    // APPROVED leave covering today → counted; PENDING covering today and APPROVED outside today → not counted
    @Test
    void getDashboard_whenApprovedLeaveCoversToday_shouldReturnEmployeesOnLeaveToday() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();
        LocalDate today = LocalDate.now();

        // Jan: APPROVED, covers today → should be counted
        persistLeave(jan, LeaveRequestStatus.APPROVED, today.minusDays(1), today.plusDays(1));

        // Anna: PENDING, covers today → should NOT be counted
        persistLeave(anna, LeaveRequestStatus.PENDING, today, today.plusDays(3));

        // Jan: APPROVED, outside today → should NOT be counted
        persistLeave(jan, LeaveRequestStatus.APPROVED, today.plusMonths(1), today.plusMonths(1).plusDays(4));

        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeesOnLeaveToday").value(1));
    }

    // Same employee with two overlapping APPROVED leaves covering today → counted as 1 (distinct)
    @Test
    void getDashboard_whenSameEmployeeHasTwoApprovedLeavesToday_shouldCountAsOne() throws Exception {
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        LocalDate today = LocalDate.now();

        persistLeave(jan, LeaveRequestStatus.APPROVED, today.minusDays(2), today.plusDays(2));
        persistLeave(jan, LeaveRequestStatus.APPROVED, today.minusDays(1), today.plusDays(1));

        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeesOnLeaveToday").value(1));
    }

    private LeaveRequest persistLeave(Employee employee, LeaveRequestStatus status,
                                      LocalDate startDate, LocalDate endDate) {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setType(LeaveType.HOLIDAY);
        lr.setStartDate(startDate);
        lr.setEndDate(endDate);
        lr.setStatus(status);
        return leaveRequestRepository.save(lr);
    }
}
