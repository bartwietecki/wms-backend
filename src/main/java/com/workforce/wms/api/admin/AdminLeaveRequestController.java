package com.workforce.wms.api.admin;

import com.workforce.wms.dto.leaverequest.LeaveRequestResponse;
import com.workforce.wms.entity.LeaveRequestStatus;
import com.workforce.wms.service.LeaveRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/leave-requests")
public class AdminLeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public AdminLeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    public Page<LeaveRequestResponse> getAll(
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return leaveRequestService.findAllFiltered(status, employeeId, from, to, pageable);
    }

    @PostMapping("/{id}/approve")
    public LeaveRequestResponse approve(@PathVariable Long id) {
        return leaveRequestService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public LeaveRequestResponse reject(@PathVariable Long id) {
        return leaveRequestService.reject(id);
    }
}
