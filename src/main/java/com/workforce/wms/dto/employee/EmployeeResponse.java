package com.workforce.wms.dto.employee;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String position,
        String employmentType,
        boolean active
) {}
