package com.workforce.wms.api.admin;

import com.workforce.wms.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminDashboardIntegrationTest extends AbstractIntegrationTest {

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

    // Leave Requests module not implemented yet - always 0
    @Test
    void getDashboard_employeesOnLeaveToday_shouldAlwaysReturnZero() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeesOnLeaveToday").value(0));
    }
}
