package com.workforce.wms.api.employee;

import com.workforce.wms.dto.report.MonthlyReportDetailResponse;
import com.workforce.wms.dto.report.MonthlyReportPreviewResponse;
import com.workforce.wms.dto.report.MonthlyReportResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.MonthlyReportPdfService;
import com.workforce.wms.service.MonthlyWorkReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final MonthlyWorkReportService monthlyWorkReportService;
    private final MonthlyReportPdfService monthlyReportPdfService;
    private final CurrentUserService currentUserService;

    public MonthlyReportController(MonthlyWorkReportService monthlyWorkReportService,
                                   MonthlyReportPdfService monthlyReportPdfService,
                                   CurrentUserService currentUserService) {
        this.monthlyWorkReportService = monthlyWorkReportService;
        this.monthlyReportPdfService = monthlyReportPdfService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/monthly/preview")
    public MonthlyReportPreviewResponse preview(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return monthlyWorkReportService.preview(resolveCurrentEmployee(), year, month);
    }

    @PostMapping("/monthly/submit")
    public MonthlyReportResponse submit(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return monthlyWorkReportService.submit(resolveCurrentEmployee(), year, month);
    }

    @GetMapping("/my")
    public List<MonthlyReportResponse> myReports() {
        return monthlyWorkReportService.myReports(resolveCurrentEmployee());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        Employee employee = resolveCurrentEmployee();
        MonthlyReportDetailResponse detail = monthlyWorkReportService.getEmployeeReportDetail(employee, id);
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

    private Employee resolveCurrentEmployee() {
        return currentUserService.getCurrentEmployee()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Authenticated user has no employee profile"));
    }
}
