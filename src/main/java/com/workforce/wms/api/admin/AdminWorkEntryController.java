package com.workforce.wms.api.admin;

import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.service.WorkEntryService;
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

    @PostMapping("/{id}/approve")
    public WorkEntryResponse approve(@PathVariable Long id) {
        return workEntryService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public WorkEntryResponse reject(@PathVariable Long id) {
        return workEntryService.reject(id);
    }
}