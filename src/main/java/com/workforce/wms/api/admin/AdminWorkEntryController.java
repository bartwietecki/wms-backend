package com.workforce.wms.api.admin;

import com.workforce.wms.dto.workentry.RejectWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.dto.workentry.WorkEntryStatusHistoryResponse;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.service.WorkEntryService;
import com.workforce.wms.service.WorkEntryStatusHistoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/work-entries")
public class AdminWorkEntryController {

    private final WorkEntryService workEntryService;
    private final WorkEntryStatusHistoryService historyService;

    public AdminWorkEntryController(WorkEntryService workEntryService, WorkEntryStatusHistoryService historyService) {
        this.workEntryService = workEntryService;
        this.historyService = historyService;
    }

    @GetMapping
    public Page<WorkEntryResponse> getAll(
            @RequestParam(required = false) WorkEntryStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "workDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return workEntryService.findAllFiltered(status, employeeId, from, to, pageable);
    }

    @PutMapping("/{id}")
    public WorkEntryResponse update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateWorkEntryRequest request) {
        return workEntryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workEntryService.delete(id);
    }

    @PostMapping("/{id}/approve")
    public WorkEntryResponse approve(@PathVariable Long id) {
        return workEntryService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public WorkEntryResponse reject(@PathVariable Long id,
                                    @Valid @RequestBody RejectWorkEntryRequest request) {
        return workEntryService.reject(id, request.reason());
    }

    @GetMapping("/{id}/history")
    public List<WorkEntryStatusHistoryResponse> getHistory(@PathVariable Long id) {
        return historyService.getHistory(id);
    }
}