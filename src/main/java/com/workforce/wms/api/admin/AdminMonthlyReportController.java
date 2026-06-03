package com.workforce.wms.api.admin;

import com.workforce.wms.dto.report.MonthlyReportDetailResponse;
import com.workforce.wms.dto.report.MonthlyReportResponse;
import com.workforce.wms.dto.report.RejectMonthlyReportRequest;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import com.workforce.wms.service.MonthlyWorkReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminMonthlyReportController {

    private final MonthlyWorkReportService monthlyWorkReportService;

    public AdminMonthlyReportController(MonthlyWorkReportService monthlyWorkReportService) {
        this.monthlyWorkReportService = monthlyWorkReportService;
    }

    @GetMapping
    public Page<MonthlyReportResponse> getAll(
            @RequestParam(required = false) MonthlyWorkReportStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return monthlyWorkReportService.findAllFiltered(status, employeeId, year, month, pageable);
    }

    @GetMapping("/{id}")
    public MonthlyReportDetailResponse getById(@PathVariable Long id) {
        return monthlyWorkReportService.getAdminReportDetail(id);
    }

    @PostMapping("/{id}/approve")
    public MonthlyReportResponse approve(@PathVariable Long id) {
        return monthlyWorkReportService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public MonthlyReportResponse reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectMonthlyReportRequest request
    ) {
        return monthlyWorkReportService.reject(id, request.adminComment());
    }
}
