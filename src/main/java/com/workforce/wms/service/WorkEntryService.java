package com.workforce.wms.service;

import com.workforce.wms.common.error.EmployeeNotFoundException;
import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
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

        return workEntryRepository.findAllByEmployeeIdAndWorkDateBetween(employeeId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
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

    private WorkEntryResponse toResponse(WorkEntry workEntry) {
        return new WorkEntryResponse(
                workEntry.getId(),
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
