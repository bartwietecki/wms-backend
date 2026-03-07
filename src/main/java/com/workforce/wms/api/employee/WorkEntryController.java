package com.workforce.wms.api.employee;

import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.service.WorkEntryService;
import org.springframework.format.annotation.DateTimeFormat;
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
}
