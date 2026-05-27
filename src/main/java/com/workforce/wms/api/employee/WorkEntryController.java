package com.workforce.wms.api.employee;

import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.WorkEntryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/work-entries")
public class WorkEntryController {

    private final WorkEntryService workEntryService;
    private final CurrentUserService currentUserService;

    public WorkEntryController(WorkEntryService workEntryService, CurrentUserService currentUserService) {
        this.workEntryService = workEntryService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public WorkEntryResponse create(@RequestBody CreateWorkEntryRequest request) {
        return workEntryService.create(resolveCurrentEmployee(), request);
    }

    @GetMapping("/my")
    public List<WorkEntryResponse> getMyWorkEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return workEntryService.myEntries(resolveCurrentEmployee(), from, to);
    }

    @PutMapping("/{id}")
    public WorkEntryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkEntryRequest request
    ) {
        return workEntryService.updateOwnEntry(resolveCurrentEmployee(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workEntryService.deleteOwnEntry(resolveCurrentEmployee(), id);
    }

    private Employee resolveCurrentEmployee() {
        return currentUserService.getCurrentEmployee()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Authenticated user has no employee profile"));
    }
}
