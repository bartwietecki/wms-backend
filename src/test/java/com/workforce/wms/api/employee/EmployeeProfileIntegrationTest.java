package com.workforce.wms.api.employee;

import com.workforce.wms.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeProfileIntegrationTest extends AbstractIntegrationTest {

    // Unauthenticated → 401
    @Test
    void getProfile_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employee/profile"))
                .andExpect(status().isUnauthorized());
    }

    // Authenticated employee → 200, returns own profile fields
    @Test
    void getProfile_whenAuthenticated_shouldReturnOwnProfile() throws Exception {
        mockMvc.perform(get("/api/employee/profile")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan@test.local"))
                .andExpect(jsonPath("$.employmentType").value("FULL_TIME"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // PUT with valid payload → 200, name updated, email preserved
    @Test
    void updateProfile_withValidPayload_shouldUpdateNameAndPreserveEmail() throws Exception {
        String body = """
                {
                  "firstName": "Janek",
                  "lastName": "Kowalski-Nowak"
                }
                """;

        mockMvc.perform(put("/api/employee/profile")
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Janek"))
                .andExpect(jsonPath("$.lastName").value("Kowalski-Nowak"))
                .andExpect(jsonPath("$.email").value("jan@test.local"));
    }

    // PUT with blank firstName → 400
    @Test
    void updateProfile_whenFirstNameBlank_shouldReturn400() throws Exception {
        String body = """
                {
                  "firstName": "",
                  "lastName": "Kowalski"
                }
                """;

        mockMvc.perform(put("/api/employee/profile")
                        .with(httpBasic(JAN, EMP_PASS))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
