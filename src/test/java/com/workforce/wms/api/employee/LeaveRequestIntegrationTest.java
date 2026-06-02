package com.workforce.wms.api.employee;

import com.workforce.wms.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeaveRequestIntegrationTest extends AbstractIntegrationTest {

    // Unauthenticated → 401
    @Test
    void createLeaveRequest_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(APPLICATION_JSON)
                        .content(holidayBody()))
                .andExpect(status().isUnauthorized());
    }

    // Employee creates leave request → 201, PENDING, correct fields
    @Test
    void createLeaveRequest_whenEmployee_shouldReturnCreatedWithPendingStatus() throws Exception {
        mockMvc.perform(post("/api/leave-requests")
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(holidayBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("HOLIDAY"))
                .andExpect(jsonPath("$.startDate").value("2026-07-01"))
                .andExpect(jsonPath("$.endDate").value("2026-07-05"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.reason").value("Summer vacation"))
                .andExpect(jsonPath("$.employeeName").value("Jan Kowalski"));
    }

    // Employee lists own requests — sees only their own
    @Test
    void getMyRequests_shouldReturnOnlyOwnRequests() throws Exception {
        // given - Jan creates 2 requests, Anna creates 1
        mockMvc.perform(post("/api/leave-requests")
                .with(httpBasic(JAN, EMP_PASS))
                .contentType(APPLICATION_JSON)
                .content(holidayBody()));
        mockMvc.perform(post("/api/leave-requests")
                .with(httpBasic(JAN, EMP_PASS))
                .contentType(APPLICATION_JSON)
                .content(sickLeaveBody()));
        mockMvc.perform(post("/api/leave-requests")
                .with(httpBasic(ANNA, EMP_PASS))
                .contentType(APPLICATION_JSON)
                .content(holidayBody()));

        // when - Jan lists his requests
        mockMvc.perform(get("/api/leave-requests/my")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // Employee cannot access admin leave-requests endpoint → 403
    @Test
    void adminLeaveRequests_whenCalledByEmployee_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/leave-requests")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isForbidden());
    }

    // startDate after endDate → 400
    @Test
    void createLeaveRequest_whenStartDateAfterEndDate_shouldReturn400() throws Exception {
        String body = """
                {
                  "type": "HOLIDAY",
                  "startDate": "2026-07-10",
                  "endDate": "2026-07-01"
                }
                """;

        mockMvc.perform(post("/api/leave-requests")
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private String holidayBody() {
        return """
                {
                  "type": "HOLIDAY",
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-05",
                  "reason": "Summer vacation"
                }
                """;
    }

    private String sickLeaveBody() {
        return """
                {
                  "type": "SICK_LEAVE",
                  "startDate": "2026-08-01",
                  "endDate": "2026-08-03"
                }
                """;
    }
}
