package com.workforce.wms.api.admin;

import com.workforce.wms.dto.report.MonthlyReportDetailResponse;
import com.workforce.wms.dto.report.MonthlyReportResponse;
import com.workforce.wms.dto.report.RejectMonthlyReportRequest;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import com.workforce.wms.service.MonthlyReportPdfService;
import com.workforce.wms.service.MonthlyWorkReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminMonthlyReportController {

    private final MonthlyWorkReportService monthlyWorkReportService;
    private final MonthlyReportPdfService monthlyReportPdfService;

    public AdminMonthlyReportController(MonthlyWorkReportService monthlyWorkReportService,
                                        MonthlyReportPdfService monthlyReportPdfService) {
        this.monthlyWorkReportService = monthlyWorkReportService;
        this.monthlyReportPdfService = monthlyReportPdfService;
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

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        MonthlyReportDetailResponse detail = monthlyWorkReportService.getAdminReportDetail(id);
        byte[] pdf = monthlyReportPdfService.generate(detail);

        String employeeSlug = detail.employeeName()
                .toLowerCase()
                .replaceAll("\\s+", "-");

        String filename =
                "monthly-report-"
                        + employeeSlug
                        + "-"
                        + String.format("%02d", detail.month())
                        + "-"
                        + detail.year() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
