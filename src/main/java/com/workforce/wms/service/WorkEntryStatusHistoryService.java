package com.workforce.wms.service;

import com.workforce.wms.dto.workentry.WorkEntryStatusHistoryResponse;
import com.workforce.wms.entity.User;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.entity.WorkEntryStatusHistory;
import com.workforce.wms.repository.WorkEntryStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class WorkEntryStatusHistoryService {

    private final WorkEntryStatusHistoryRepository historyRepository;

    public WorkEntryStatusHistoryService(WorkEntryStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void saveStatusChange(WorkEntry workEntry, WorkEntryStatus oldStatus,
                                 WorkEntryStatus newStatus, User changedBy, String comment) {
        WorkEntryStatusHistory history = new WorkEntryStatusHistory();
        history.setWorkEntry(workEntry);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(OffsetDateTime.now());
        history.setChangedBy(changedBy);
        history.setComment(comment);
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<WorkEntryStatusHistoryResponse> getHistory(Long workEntryId) {
        return historyRepository.findByWorkEntryIdOrderByChangedAtAsc(workEntryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private WorkEntryStatusHistoryResponse toResponse(WorkEntryStatusHistory h) {
        String changedBy = h.getChangedBy() != null ? h.getChangedBy().getUsername() : "system";
        return new WorkEntryStatusHistoryResponse(
                h.getId(),
                h.getOldStatus(),
                h.getNewStatus(),
                h.getChangedAt(),
                changedBy,
                h.getComment()
        );
    }
}