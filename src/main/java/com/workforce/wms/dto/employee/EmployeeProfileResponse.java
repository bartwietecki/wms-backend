package com.workforce.wms.dto.employee;

public record EmployeeProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String position,
        String employmentType,
        boolean active,
        String departmentName,
        String positionName
) {}
