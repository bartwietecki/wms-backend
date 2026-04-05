package com.workforce.wms.repository;

import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkEntryRepository extends JpaRepository<WorkEntry, Long> {

    List<WorkEntry> findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    List<WorkEntry> findAllByOrderByWorkDateDesc();

    boolean existsByEmployeeId(Long employeeId);

    @Query(value = """
            SELECT w FROM WorkEntry w
            JOIN FETCH w.employee
            WHERE (:status     IS NULL OR w.status       = :status)
              AND (:employeeId IS NULL OR w.employee.id  = :employeeId)
              AND (:from       IS NULL OR w.workDate     >= :from)
              AND (:to         IS NULL OR w.workDate     <= :to)
            """,
            countQuery = """
            SELECT COUNT(w) FROM WorkEntry w
            WHERE (:status     IS NULL OR w.status       = :status)
              AND (:employeeId IS NULL OR w.employee.id  = :employeeId)
              AND (:from       IS NULL OR w.workDate     >= :from)
              AND (:to         IS NULL OR w.workDate     <= :to)
            """)
    Page<WorkEntry> findFiltered(
            @Param("status")     WorkEntryStatus status,
            @Param("employeeId") Long employeeId,
            @Param("from")       LocalDate from,
            @Param("to")         LocalDate to,
            Pageable pageable
    );
}
