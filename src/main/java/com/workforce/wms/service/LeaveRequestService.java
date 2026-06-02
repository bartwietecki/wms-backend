package com.workforce.wms.service;

import com.workforce.wms.common.error.InvalidLeaveRequestException;
import com.workforce.wms.common.error.LeaveRequestNotFoundException;
import com.workforce.wms.dto.leaverequest.CreateLeaveRequestRequest;
import com.workforce.wms.dto.leaverequest.LeaveRequestResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.LeaveRequest;
import com.workforce.wms.entity.LeaveRequestStatus;
import com.workforce.wms.repository.LeaveRequestRepository;
import com.workforce.wms.repository.LeaveRequestSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public LeaveRequestResponse create(Employee employee, CreateLeaveRequestRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new InvalidLeaveRequestException("startDate must be <= endDate");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setType(request.type());
        leaveRequest.setStartDate(request.startDate());
        leaveRequest.setEndDate(request.endDate());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest.setReason(request.reason() != null ? request.reason().trim() : null);

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> myRequests(Employee employee) {
        return leaveRequestRepository.findAllByEmployeeIdOrderByCreatedAtDesc(employee.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> findAllFiltered(LeaveRequestStatus status, Long employeeId,
                                                      LocalDate from, LocalDate to, Pageable pageable) {
        Specification<LeaveRequest> spec = Specification
                .where(LeaveRequestSpecifications.hasStatus(status))
                .and(LeaveRequestSpecifications.hasEmployeeId(employeeId))
                .and(LeaveRequestSpecifications.startDateFrom(from))
                .and(LeaveRequestSpecifications.startDateTo(to));

        return leaveRequestRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public LeaveRequestResponse approve(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException(id));

        validatePending(leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    public LeaveRequestResponse reject(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException(id));

        validatePending(leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    private void validatePending(LeaveRequest leaveRequest) {
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new InvalidLeaveRequestException(
                    "Only PENDING leave request can be changed. Current status: " + leaveRequest.getStatus()
            );
        }
    }

    private LeaveRequestResponse toResponse(LeaveRequest leaveRequest) {
        Employee employee = leaveRequest.getEmployee();
        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        return new LeaveRequestResponse(
                leaveRequest.getId(),
                employee.getId(),
                employeeName,
                leaveRequest.getType(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getStatus(),
                leaveRequest.getReason(),
                leaveRequest.getCreatedAt()
        );
    }
}
