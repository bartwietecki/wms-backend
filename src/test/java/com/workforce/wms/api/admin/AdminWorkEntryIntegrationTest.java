package com.workforce.wms.api.admin;

import com.workforce.wms.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminWorkEntryIntegrationTest extends AbstractIntegrationTest {

    // Admin lists all work entries
    @Test
    void listWorkEntries_shouldReturnAllEntries() throws Exception {
        // @BeforeEach inserts 3 entries: Jan (PENDING + APPROVED) + Anna (PENDING)
        mockMvc.perform(get("/api/admin/work-entries")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    // Admin approves PENDING entry
    @Test
    void approveWorkEntry_whenPending_shouldReturnApprovedStatus() throws Exception {
        mockMvc.perform(post("/api/admin/work-entries/" + janPendingEntryId + "/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.id").value(janPendingEntryId));
    }

    // Admin reject → status REJECTED + history record created
    @Test
    void rejectWorkEntry_shouldTransitionStatusAndCreateHistoryRecord() throws Exception {
        // given
        String rejectBody = """
                {
                  "reason": "Description too vague"
                }
                """;

        // when
        mockMvc.perform(post("/api/admin/work-entries/" + janPendingEntryId + "/reject")
                        .with(httpBasic(ADMIN, ADMIN_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(rejectBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // then (verify history record was persisted with correct fields)
        // changedBy = "system" because admin has no row in the users DB table;
        // WorkEntryService passes currentUserService.getCurrentUser().orElse(null).
        mockMvc.perform(get("/api/admin/work-entries/" + janPendingEntryId + "/history")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].oldStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].newStatus").value("REJECTED"))
                .andExpect(jsonPath("$[0].comment").value("Description too vague"))
                .andExpect(jsonPath("$[0].changedBy").value("system"));
    }
}
