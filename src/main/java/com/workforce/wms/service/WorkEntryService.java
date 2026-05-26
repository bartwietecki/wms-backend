package com.workforce.wms.service;

import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.common.error.WorkEntryAccessDeniedException;
import com.workforce.wms.common.error.WorkEntryNotFoundException;
import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.User;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.WorkEntryRepository;
import com.workforce.wms.repository.WorkEntrySpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WorkEntryService {

    private final WorkEntryRepository workEntryRepository;
    private final WorkEntryStatusHistoryService historyService;
    private final CurrentUserService currentUserService;

    public WorkEntryService(WorkEntryRepository workEntryRepository,
                            WorkEntryStatusHistoryService historyService,
                            CurrentUserService currentUserService) {
        this.workEntryRepository = workEntryRepository;
        this.historyService = historyService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public WorkEntryResponse create(Employee employee, CreateWorkEntryRequest request) {
        validateCreateRequest(request);

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
    public List<WorkEntryResponse> myEntries(Employee employee, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidWorkEntryException("from/to cannot be null");
        }
        if (from.isAfter(to)) {
            throw new InvalidWorkEntryException("from must be <= to");
        }

        return workEntryRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employee.getId(), from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<WorkEntryResponse> findAllFiltered(WorkEntryStatus status, Long employeeId,
                                                   LocalDate from, LocalDate to, Pageable pageable) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidWorkEntryException("from must be <= to");
        }

        Specification<WorkEntry> spec = Specification
                .where(WorkEntrySpecifications.hasStatus(status))
                .and(WorkEntrySpecifications.hasEmployeeId(employeeId))
                .and(WorkEntrySpecifications.workDateFrom(from))
                .and(WorkEntrySpecifications.workDateTo(to));

        return workEntryRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public WorkEntryResponse approve(Long id) {
        WorkEntry workEntry = workEntryRepository.findById(id)
                .orElseThrow(() -> new WorkEntryNotFoundException(id));

        validatePending(workEntry);

        WorkEntryStatus oldStatus = workEntry.getStatus();
        workEntry.setStatus(WorkEntryStatus.APPROVED);
        WorkEntry saved = workEntryRepository.save(workEntry);

        User currentUser = currentUserService.getCurrentUser().orElse(null);
        historyService.saveStatusChange(saved, oldStatus, WorkEntryStatus.APPROVED, currentUser, null);

        return toResponse(saved);
    }

    public WorkEntryResponse reject(Long id, String reason) {
        WorkEntry workEntry = workEntryRepository.findById(id)
                .orElseThrow(() -> new WorkEntryNotFoundException(id));

        validatePending(workEntry);

        WorkEntryStatus oldStatus = workEntry.getStatus();
        workEntry.setStatus(WorkEntryStatus.REJECTED);
        WorkEntry saved = workEntryRepository.save(workEntry);

        User currentUser = currentUserService.getCurrentUser().orElse(null);
        historyService.saveStatusChange(saved, oldStatus, WorkEntryStatus.REJECTED, currentUser, reason);

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

    public WorkEntryResponse updateOwnEntry(Employee employee, Long workEntryId, UpdateWorkEntryRequest request) {
        WorkEntry workEntry = workEntryRepository.findById(workEntryId)
                .orElseThrow(() -> new WorkEntryNotFoundException(workEntryId));

        if (!workEntry.getEmployee().getId().equals(employee.getId())) {
            throw new WorkEntryAccessDeniedException(workEntryId);
        }

        validatePending(workEntry);

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

    public void deleteOwnEntry(Employee employee, Long workEntryId) {
        WorkEntry workEntry = workEntryRepository.findById(workEntryId)
                .orElseThrow(() -> new WorkEntryNotFoundException(workEntryId));

        if (!workEntry.getEmployee().getId().equals(employee.getId())) {
            throw new WorkEntryAccessDeniedException(workEntryId);
        }

        validatePending(workEntry);

        workEntryRepository.deleteById(workEntryId);
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
