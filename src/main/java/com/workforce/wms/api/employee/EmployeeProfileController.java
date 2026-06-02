package com.workforce.wms.api.employee;

import com.workforce.wms.dto.employee.EmployeeProfileResponse;
import com.workforce.wms.dto.employee.UpdateEmployeeProfileRequest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/employee/profile")
public class EmployeeProfileController {

    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    public EmployeeProfileController(EmployeeService employeeService, CurrentUserService currentUserService) {
        this.employeeService = employeeService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public EmployeeProfileResponse getProfile() {
        return employeeService.getMyProfile(resolveCurrentEmployee());
    }

    @PutMapping
    public EmployeeProfileResponse updateProfile(@Valid @RequestBody UpdateEmployeeProfileRequest request) {
        return employeeService.updateMyProfile(resolveCurrentEmployee(), request);
    }

    private Employee resolveCurrentEmployee() {
        return currentUserService.getCurrentEmployee()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Authenticated user has no employee profile"));
    }
}
