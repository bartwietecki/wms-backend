package com.workforce.wms.api.admin;

import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.service.WorkEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/work-entries")
public class AdminWorkEntryController {

    private final WorkEntryService workEntryService;

    public AdminWorkEntryController(WorkEntryService workEntryService) {
        this.workEntryService = workEntryService;
    }

    @GetMapping
    public List<WorkEntryResponse> getAll() {
        return workEntryService.findAll();
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
    public WorkEntryResponse reject(@PathVariable Long id) {
        return workEntryService.reject(id);
    }
}