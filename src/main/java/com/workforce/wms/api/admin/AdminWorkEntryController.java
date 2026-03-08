package com.workforce.wms.api.admin;

import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.service.WorkEntryService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/work-entries")
public class AdminWorkEntryController {

    private final WorkEntryService workEntryService;

    public AdminWorkEntryController(WorkEntryService workEntryService) {
        this.workEntryService = workEntryService;
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