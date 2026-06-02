package com.workforce.wms.service;

import com.workforce.wms.common.error.EmailAlreadyExistsException;
import com.workforce.wms.common.error.EmployeeNotFoundException;
import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.dto.employee.CreateEmployeeRequest;
import com.workforce.wms.dto.employee.EmployeeProfileResponse;
import com.workforce.wms.dto.employee.EmployeeResponse;
import com.workforce.wms.dto.employee.UpdateEmployeeProfileRequest;
import com.workforce.wms.dto.employee.UpdateEmployeeRequest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final WorkEntryRepository workEntryRepository;

    public EmployeeService(EmployeeRepository employeeRepository, WorkEntryRepository workEntryRepository) {
        this.employeeRepository = employeeRepository;
        this.workEntryRepository = workEntryRepository;
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

    public void delete(Long id) {
        Employee employee = getEmployeeOrThrow(id);
        if (workEntryRepository.existsByEmployeeId(id)) {
            throw new InvalidWorkEntryException(
                    "Cannot delete employee with existing work entries. Deactivate instead."
            );
        }
        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getMyProfile(Employee employee) {
        // Re-fetch inside transaction to safely initialize lazy associations
        Employee managed = getEmployeeOrThrow(employee.getId());
        return toProfileResponse(managed);
    }

    public EmployeeProfileResponse updateMyProfile(Employee employee, UpdateEmployeeProfileRequest request) {
        Employee managed = getEmployeeOrThrow(employee.getId());

        managed.setFirstName(request.firstName());
        managed.setLastName(request.lastName());

        return toProfileResponse(employeeRepository.save(managed));
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
        if (!employee.getEmail().equalsIgnoreCase(request.email()) &&
                employeeRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPosition(request.position());
        employee.setEmploymentType(request.employmentType());
        employee.setActive(request.active());
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

    private EmployeeProfileResponse toProfileResponse(Employee employee) {
        String departmentName = (employee.getDepartment() != null)
                ? employee.getDepartment().getName()
                : null;
        String positionName = (employee.getPositionEntity() != null)
                ? employee.getPositionEntity().getName()
                : null;
        return new EmployeeProfileResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getEmploymentType(),
                employee.isActive(),
                departmentName,
                positionName
        );
    }

}
