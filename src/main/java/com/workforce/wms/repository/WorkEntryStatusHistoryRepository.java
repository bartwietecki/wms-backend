package com.workforce.wms.repository;

import com.workforce.wms.entity.WorkEntryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkEntryStatusHistoryRepository extends JpaRepository<WorkEntryStatusHistory, Long> {

    List<WorkEntryStatusHistory> findByWorkEntryIdOrderByChangedAtAsc(Long workEntryId);
}
