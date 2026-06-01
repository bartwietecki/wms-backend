package com.workforce.wms.service;

import com.workforce.wms.dto.dashboard.EmployeeDashboardResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.WorkEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final WorkEntryRepository workEntryRepository;

    public DashboardService(WorkEntryRepository workEntryRepository) {
        this.workEntryRepository = workEntryRepository;
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

        long pendingCount = workEntryRepository.countByEmployeeIdAndStatus(
                employee.getId(), WorkEntryStatus.PENDING);
        long approvedCount = workEntryRepository.countByEmployeeIdAndStatus(
                employee.getId(), WorkEntryStatus.APPROVED);
        long rejectedCount = workEntryRepository.countByEmployeeIdAndStatus(
                employee.getId(), WorkEntryStatus.REJECTED);

        // TODO replace with real value when Leave Requests module is implemented
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
