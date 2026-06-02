package com.workforce.wms.repository;

import com.workforce.wms.entity.LeaveRequest;
import com.workforce.wms.entity.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    List<LeaveRequest> findAllByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("""
            select count(distinct lr.employee.id)
            from LeaveRequest lr
            where lr.status = :status
              and lr.startDate <= :today
              and lr.endDate >= :today
            """)
    long countDistinctEmployeesOnLeaveToday(
            @Param("status") LeaveRequestStatus status,
            @Param("today") LocalDate today
    );

    @EntityGraph(attributePaths = "employee")
    Page<LeaveRequest> findAll(Specification<LeaveRequest> spec, Pageable pageable);
}
