package com.workforce.wms.api.employee;

import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.service.WorkEntryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/work-entries")
public class WorkEntryController {

    private final WorkEntryService workEntryService;

    public WorkEntryController(WorkEntryService workEntryService) {
        this.workEntryService = workEntryService;
    }

    @PostMapping
    public WorkEntryResponse create(
            @RequestHeader("X-EMPLOYEE-ID") Long employeeId,
            @RequestBody CreateWorkEntryRequest request
    ) {
        return workEntryService.create(employeeId, request);
    }

    @GetMapping("/my")
    public List<WorkEntryResponse> getMyWorkEntries(
            @RequestHeader("X-EMPLOYEE-ID") Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return workEntryService.myEntries(employeeId, from, to);
    }

    @PutMapping("/{id}")
    public WorkEntryResponse update(
            @RequestHeader("X-EMPLOYEE-ID") Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkEntryRequest request
    ) {
        return workEntryService.updateOwnEntry(employeeId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-EMPLOYEE-ID") Long employeeId,
            @PathVariable Long id
    ) {
        workEntryService.deleteOwnEntry(employeeId, id);
    }
}
