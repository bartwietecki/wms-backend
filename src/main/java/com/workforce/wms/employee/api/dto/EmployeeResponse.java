package com.workforce.wms.employee.api.dto;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String position,
        String employmentType,
        boolean active
) {}
