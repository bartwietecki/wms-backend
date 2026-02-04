package com.workforce.wms.employee.service;

import com.workforce.wms.common.error.EmailAlreadyExistsException;
import com.workforce.wms.common.error.EmployeeNotFoundException;
import com.workforce.wms.employee.api.dto.CreateEmployeeRequest;
import com.workforce.wms.employee.api.dto.EmployeeResponse;
import com.workforce.wms.employee.api.dto.UpdateEmployeeRequest;
import com.workforce.wms.employee.entity.Employee;
import com.workforce.wms.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findAll(Boolean active, Pageable pageable) {
        Page<Employee> page = (active == null)
                ? employeeRepository.findAll(pageable)
                : employeeRepository.findAllByActive(active, pageable);

        return page.map(this::toResponse);
    }

    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Employee employee = new Employee();
        mapFromCreate(employee, request);

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return toResponse(getEmployeeOrThrow(id));
    }

    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = getEmployeeOrThrow(id);
        applyUpdate(employee, request);

        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse activate(Long id) {
        return setActive(id, true);
    }

    public EmployeeResponse deactivate(Long id) {
        return setActive(id, false);
    }

    private EmployeeResponse setActive(Long id, boolean isActive) {
        Employee employee = getEmployeeOrThrow(id);
        employee.setActive(isActive);
        return toResponse(employee);
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private void mapFromCreate(Employee employee, CreateEmployeeRequest request) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPosition(request.position());
        employee.setEmploymentType(request.employmentType());
        employee.setActive(true);
    }

    private void applyUpdate(Employee employee, UpdateEmployeeRequest request) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setPosition(request.position());
        employee.setEmploymentType(request.employmentType());
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getEmploymentType(),
                employee.isActive()
        );
    }

}
