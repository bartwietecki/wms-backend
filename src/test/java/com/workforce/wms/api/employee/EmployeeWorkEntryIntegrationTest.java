package com.workforce.wms.api.employee;

import com.workforce.wms.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeWorkEntryIntegrationTest extends AbstractIntegrationTest {

    // Unauthenticated → 401
    @Test
    void employeeEndpoint_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/work-entries/my?from=2026-01-01&to=2026-12-31"))
                .andExpect(status().isUnauthorized());
    }

    // Employee role → admin endpoint → 403
    @Test
    void adminEndpoint_whenCalledByEmployee_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/work-entries")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isForbidden());
    }

    // Employee creates own work entry → 200, PENDING, correct name
    @Test
    void createWorkEntry_shouldReturn200WithPendingStatus() throws Exception {
        // given
        // POST /api/work-entries returns 200, not 201 — WorkEntryController.create()
        // has no @ResponseStatus(CREATED). Asserting current behaviour, not convention.
        String body = """
                {
                  "workDate": "2026-03-15",
                  "minutes": 480,
                  "description": "Integration test entry"
                }
                """;

        // when / then
        mockMvc.perform(post("/api/work-entries")
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Jan Kowalski"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.minutes").value(480))
                .andExpect(jsonPath("$.workDate").value("2026-03-15"));
    }

    // Employee lists only own entries (Anna's entry must not appear)
    @Test
    void listMyWorkEntries_shouldReturnOnlyOwnEntries() throws Exception {
        // @BeforeEach inserts: Jan PENDING + Jan APPROVED + Anna PENDING.
        // A full-year range covers the 2026-03-01 work date of all three entries.
        mockMvc.perform(get("/api/work-entries/my?from=2026-01-01&to=2026-12-31")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].employeeName", everyItem(is("Jan Kowalski"))));
    }

    // Employee cannot update another employee's entry → 403
    @Test
    void updateWorkEntry_whenBelongsToAnotherEmployee_shouldReturn403() throws Exception {
        // given
        String body = """
                {
                  "workDate": "2026-03-01",
                  "minutes": 120,
                  "description": "Attempted hijack"
                }
                """;

        // when / then
        mockMvc.perform(put("/api/work-entries/" + annaPendingEntryId)
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("WORK_ENTRY_ACCESS_DENIED"));
    }

    // Employee cannot delete another employee's entry → 403
    @Test
    void deleteWorkEntry_whenBelongsToAnotherEmployee_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/work-entries/" + annaPendingEntryId)
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("WORK_ENTRY_ACCESS_DENIED"));
    }

    // Employee cannot update own non-PENDING entry → 400
    @Test
    void updateWorkEntry_whenEntryIsNotPending_shouldReturn400() throws Exception {
        // given
        String body = """
                {
                  "workDate": "2026-03-01",
                  "minutes": 120,
                  "description": "Should be rejected"
                }
                """;

        // when / then
        mockMvc.perform(put("/api/work-entries/" + janApprovedEntryId)
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WORK_ENTRY"));
    }

    // Employee cannot delete own non-PENDING entry → 400
    @Test
    void deleteWorkEntry_whenEntryIsNotPending_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/work-entries/" + janApprovedEntryId)
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WORK_ENTRY"));
    }
}
