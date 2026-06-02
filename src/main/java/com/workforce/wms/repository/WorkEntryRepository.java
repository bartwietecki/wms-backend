package com.workforce.wms.repository;

import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface WorkEntryRepository extends JpaRepository<WorkEntry, Long>, JpaSpecificationExecutor<WorkEntry> {

    List<WorkEntry> findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    long countByEmployeeIdAndStatusAndWorkDateBetween(
            Long employeeId,
            WorkEntryStatus status,
            LocalDate from,
            LocalDate to
    );

    List<WorkEntry> findAllByOrderByWorkDateDesc();

    boolean existsByEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = "employee")
    Page<WorkEntry> findAll(Specification<WorkEntry> spec, Pageable pageable);
}
