package com.workforce.wms.employee.service;

import com.workforce.wms.common.error.EmailAlreadyExistsException;
import com.workforce.wms.common.error.EmployeeNotFoundException;
import com.workforce.wms.employee.api.dto.CreateEmployeeRequest;
import com.workforce.wms.employee.api.dto.EmployeeResponse;
import com.workforce.wms.employee.entity.Employee;
import com.workforce.wms.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EmployeeResponse create(CreateEmployeeRequest createEmployeeRequest) {
        if (employeeRepository.existsByEmail(createEmployeeRequest.email())) {
            throw new EmailAlreadyExistsException(createEmployeeRequest.email());
        }

        Employee employee = new Employee();
        employee.setFirstName(createEmployeeRequest.firstName());
        employee.setLastName(createEmployeeRequest.lastName());
        employee.setEmail(createEmployeeRequest.email());
        employee.setPosition(createEmployeeRequest.position());
        employee.setEmploymentType(createEmployeeRequest.employmentType());
        employee.setActive(true);

        return toResponse(employeeRepository.save(employee));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(), employee.getFirstName(), employee.getLastName(),
                employee.getEmail(), employee.getPosition(), employee.getEmploymentType(),
                employee.isActive()
        );
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return toResponse(employee);
    }

}
