package com.workforce.wms.repository;

import com.workforce.wms.entity.MonthlyWorkReport;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MonthlyWorkReportRepository extends JpaRepository<MonthlyWorkReport, Long>,
        JpaSpecificationExecutor<MonthlyWorkReport> {

    Optional<MonthlyWorkReport> findByEmployeeIdAndYearAndMonth(Long employeeId, int year, int month);

    List<MonthlyWorkReport> findAllByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);

    @EntityGraph(attributePaths = "employee")
    Page<MonthlyWorkReport> findAll(Specification<MonthlyWorkReport> spec, Pageable pageable);
}
