package com.workforce.wms.service;

import com.workforce.wms.common.error.EmployeeNotFoundException;
import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.common.error.WorkEntryNotFoundException;
import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WorkEntryService {

    private final WorkEntryRepository workEntryRepository;
    private final EmployeeRepository employeeRepository;

    public WorkEntryService(WorkEntryRepository workEntryRepository, EmployeeRepository employeeRepository) {
        this.workEntryRepository = workEntryRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public WorkEntryResponse create(Long employeeId, CreateWorkEntryRequest request) {
        validateCreateRequest(request);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        WorkEntry workEntry = new WorkEntry();
        workEntry.setEmployee(employee);
        workEntry.setWorkDate(request.workDate());
        workEntry.setMinutes(request.minutes());
        workEntry.setDescription(trimToNull(request.description()));
        workEntry.setStatus(WorkEntryStatus.PENDING);

        WorkEntry saved = workEntryRepository.save(workEntry);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkEntryResponse> myEntries(Long employeeId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidWorkEntryException("from/to cannot be null");
        }
        if (from.isAfter(to)) {
            throw new InvalidWorkEntryException("from must be <= to");
        }

        return workEntryRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employeeId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkEntryResponse> findAll() {
        return workEntryRepository.findAllByOrderByWorkDateDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkEntryResponse approve(Long id) {
        WorkEntry workEntry = workEntryRepository.findById(id)
                .orElseThrow(() -> new WorkEntryNotFoundException(id));

        validatePending(workEntry);

        workEntry.setStatus(WorkEntryStatus.APPROVED);
        WorkEntry saved = workEntryRepository.save(workEntry);

        return toResponse(saved);
    }

    public WorkEntryResponse reject(Long id) {
        WorkEntry workEntry = workEntryRepository.findById(id)
                .orElseThrow(() -> new WorkEntryNotFoundException(id));

        validatePending(workEntry);

        workEntry.setStatus(WorkEntryStatus.REJECTED);
        WorkEntry saved = workEntryRepository.save(workEntry);

        return toResponse(saved);
    }

    public WorkEntryResponse update(Long id, UpdateWorkEntryRequest request) {
        WorkEntry workEntry = workEntryRepository.findById(id)
                .orElseThrow(() -> new WorkEntryNotFoundException(id));

        workEntry.setWorkDate(request.workDate());
        workEntry.setMinutes(request.minutes());
        workEntry.setDescription(trimToNull(request.description()));

        return toResponse(workEntryRepository.save(workEntry));
    }

    public void delete(Long id) {
        if (!workEntryRepository.existsById(id)) {
            throw new WorkEntryNotFoundException(id);
        }
        workEntryRepository.deleteById(id);
    }

    private void validateCreateRequest(CreateWorkEntryRequest request) {
        if (request == null) {
            throw new InvalidWorkEntryException("request cannot be null");
        }
        if (request.workDate() == null) {
            throw new InvalidWorkEntryException("workDate cannot be null");
        }
        if (request.minutes() <= 0) {
            throw new InvalidWorkEntryException("minutes must be > 0");
        }
    }

    private void validatePending(WorkEntry workEntry) {
        if (workEntry.getStatus() != WorkEntryStatus.PENDING) {
            throw new InvalidWorkEntryException(
                    "Only PENDING work entry can be changed. Current status: " + workEntry.getStatus()
            );
        }
    }

    private WorkEntryResponse toResponse(WorkEntry workEntry) {
        Employee employee = workEntry.getEmployee();
        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        return new WorkEntryResponse(
                workEntry.getId(),
                employee.getId(),
                employeeName,
                workEntry.getWorkDate(),
                workEntry.getMinutes(),
                workEntry.getDescription(),
                workEntry.getStatus()
        );
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
