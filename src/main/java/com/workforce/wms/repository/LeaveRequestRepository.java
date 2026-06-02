package com.workforce.wms.repository;

import com.workforce.wms.entity.LeaveRequest;
import com.workforce.wms.entity.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    List<LeaveRequest> findAllByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Used by admin dashboard: count approved leaves that cover a specific date
    long countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LeaveRequestStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    @EntityGraph(attributePaths = "employee")
    Page<LeaveRequest> findAll(Specification<LeaveRequest> spec, Pageable pageable);
}
