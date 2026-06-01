package com.workforce.wms.api.employee;

import com.workforce.wms.AbstractIntegrationTest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeDashboardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkEntryRepository workEntryRepository;

    // Unauthenticated → 401
    @Test
    void getDashboard_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employee/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // Authenticated employee → 200, status counts reflect only own entries
    @Test
    void getDashboard_shouldReturnCorrectStatusCounts() throws Exception {
        mockMvc.perform(get("/api/employee/dashboard")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingEntriesCount").value(1))
                .andExpect(jsonPath("$.approvedEntriesCount").value(1))
                .andExpect(jsonPath("$.rejectedEntriesCount").value(0))
                .andExpect(jsonPath("$.leaveDaysRemaining").value(0));
    }

    // totalHoursThisMonth sums only the current calendar month's entries
    @Test
    void getDashboard_shouldCalculateTotalHoursForCurrentMonthOnly() throws Exception {
        // given - two entries in the current month: 480 + 240 = 720 min = 12 hours
        // The base fixture entries (2026-03-01) are in March and can NOT be counted
        Employee jan = employeeRepository.findByUser_Username(JAN).orElseThrow();
        persistCurrentMonthEntry(jan, 480); // 8 hours
        persistCurrentMonthEntry(jan, 240); // 4 hours

        // when / then
        mockMvc.perform(get("/api/employee/dashboard")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHoursThisMonth").value(12));
    }

    // Monthly hours are isolated to the authenticated employee - Anna's entries don't count
    @Test
    void getDashboard_totalHoursThisMonth_shouldNotIncludeOtherEmployeesEntries() throws Exception {
        // given - add a current-month entry for Anna only
        Employee anna = employeeRepository.findByUser_Username(ANNA).orElseThrow();
        persistCurrentMonthEntry(anna, 480);

        // when / then - Jan has no current-month entries, so hours must be 0
        mockMvc.perform(get("/api/employee/dashboard")
                        .with(httpBasic(JAN, EMP_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHoursThisMonth").value(0));
    }

    // saves a PENDING work entry for the first day of the current month
    private void persistCurrentMonthEntry(Employee employee, int minutes) {
        WorkEntry entry = new WorkEntry();
        entry.setEmployee(employee);
        entry.setWorkDate(LocalDate.now().withDayOfMonth(1));
        entry.setMinutes(minutes);
        entry.setStatus(WorkEntryStatus.PENDING);
        workEntryRepository.save(entry);
    }
}
