package com.workforce.wms.service;

import com.workforce.wms.dto.dashboard.AdminDashboardResponse;
import com.workforce.wms.dto.dashboard.EmployeeDashboardResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.LeaveRequestStatus;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.LeaveRequestRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final WorkEntryRepository workEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public DashboardService(WorkEntryRepository workEntryRepository, EmployeeRepository employeeRepository,
                            LeaveRequestRepository leaveRequestRepository) {
        this.workEntryRepository = workEntryRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public AdminDashboardResponse getAdminDashboard() {
        long pendingApprovalsCount = workEntryRepository.countByStatus(WorkEntryStatus.PENDING);
        long activeEmployeesCount = employeeRepository.countByActive(true);
        long employeesOnLeaveToday = leaveRequestRepository.countDistinctEmployeesOnLeaveToday(
                LeaveRequestStatus.APPROVED, LocalDate.now());

        return new AdminDashboardResponse(pendingApprovalsCount, activeEmployeesCount, employeesOnLeaveToday);
    }

    public EmployeeDashboardResponse getEmployeeDashboard(Employee employee) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        int totalMinutesThisMonth = workEntryRepository
                .findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
                        employee.getId(), monthStart, monthEnd)
                .stream()
                .mapToInt(entry -> entry.getMinutes())
                .sum();

        int totalHoursThisMonth = totalMinutesThisMonth / 60;

        long pendingCount = workEntryRepository.countByEmployeeIdAndStatusAndWorkDateBetween(
                employee.getId(), WorkEntryStatus.PENDING, monthStart, monthEnd);
        long approvedCount = workEntryRepository.countByEmployeeIdAndStatusAndWorkDateBetween(
                employee.getId(), WorkEntryStatus.APPROVED, monthStart, monthEnd);
        long rejectedCount = workEntryRepository.countByEmployeeIdAndStatusAndWorkDateBetween(
                employee.getId(), WorkEntryStatus.REJECTED, monthStart, monthEnd);

        // The Leave Requests module is implemented, but there is no leave-allowance concept yet,
        // so a real remaining balance cannot be computed.
        // Hardcoded until an allowance/entitlement model is introduced.
        int leaveDaysRemaining = 0;

        return new EmployeeDashboardResponse(
                totalHoursThisMonth,
                pendingCount,
                approvedCount,
                rejectedCount,
                leaveDaysRemaining
        );
    }
}
