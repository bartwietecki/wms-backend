package com.workforce.wms.employee.api.employee;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/profile")
public class EmployeeProfileController {

    @GetMapping
    public Map<String, String> getEmployeeProfile(Principal principal) {
        return Map.of("username", principal.getName());
    }
}
