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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminLeaveRequestIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Admin lists all leave requests → 200
    @Test
    void getAll_whenAdmin_shouldReturnAllRequests() throws Exception {
        // given - Jan and Anna each have one pending leave request
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();
        persistLeaveRequest(jan, LeaveRequestStatus.PENDING);
        persistLeaveRequest(anna, LeaveRequestStatus.PENDING);

        // when / then
        mockMvc.perform(get("/api/admin/leave-requests")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // Admin approves PENDING leave request → 200, status APPROVED
    @Test
    void approve_whenPending_shouldReturnApproved() throws Exception {
        // given
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        LeaveRequest lr = persistLeaveRequest(jan, LeaveRequestStatus.PENDING);

        // when / then
        mockMvc.perform(post("/api/admin/leave-requests/" + lr.getId() + "/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.id").value(lr.getId()));
    }

    // Admin rejects PENDING leave request → 200, status REJECTED
    @Test
    void reject_whenPending_shouldReturnRejected() throws Exception {
        // given
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        LeaveRequest lr = persistLeaveRequest(jan, LeaveRequestStatus.PENDING);

        // when / then
        mockMvc.perform(post("/api/admin/leave-requests/" + lr.getId() + "/reject")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    // Approve non-PENDING → 400
    @Test
    void approve_whenAlreadyApproved_shouldReturn400() throws Exception {
        // given
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        LeaveRequest lr = persistLeaveRequest(jan, LeaveRequestStatus.APPROVED);

        mockMvc.perform(post("/api/admin/leave-requests/" + lr.getId() + "/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isBadRequest());
    }

    // Reject non-PENDING → 400
    @Test
    void reject_whenAlreadyRejected_shouldReturn400() throws Exception {
        // given
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        LeaveRequest lr = persistLeaveRequest(jan, LeaveRequestStatus.REJECTED);

        mockMvc.perform(post("/api/admin/leave-requests/" + lr.getId() + "/reject")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isBadRequest());
    }

    // Not found → 404
    @Test
    void approve_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/admin/leave-requests/99999/approve")
                        .with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isNotFound());
    }

    private LeaveRequest persistLeaveRequest(Employee employee, LeaveRequestStatus status) {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setType(LeaveType.HOLIDAY);
        lr.setStartDate(LocalDate.of(2026, 7, 1));
        lr.setEndDate(LocalDate.of(2026, 7, 5));
        lr.setStatus(status);
        return leaveRequestRepository.save(lr);
    }
}
