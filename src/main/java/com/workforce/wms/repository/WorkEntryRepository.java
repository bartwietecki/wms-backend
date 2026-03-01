package com.workforce.wms.repository;

import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkEntryRepository extends JpaRepository<WorkEntry, Long> {

    List<WorkEntry> findAllByEmployeeIdAndWorkDateBetween(
            Long employeeId,
            LocalDate from,
            LocalDate to
    );

    List<WorkEntry> findAllByStatus(WorkEntryStatus status);
}