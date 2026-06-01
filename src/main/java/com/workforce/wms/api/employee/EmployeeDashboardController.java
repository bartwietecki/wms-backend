package com.workforce.wms.api.employee;

import com.workforce.wms.dto.dashboard.EmployeeDashboardResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/employee/dashboard")
public class EmployeeDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public EmployeeDashboardController(DashboardService dashboardService, CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public EmployeeDashboardResponse getDashboard() {
        return dashboardService.getEmployeeDashboard(resolveCurrentEmployee());
    }

    private Employee resolveCurrentEmployee() {
        return currentUserService.getCurrentEmployee()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Authenticated user has no employee profile"));
    }
}
