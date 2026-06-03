package com.workforce.wms.api.employee;

import com.workforce.wms.dto.report.MonthlyReportPreviewResponse;
import com.workforce.wms.dto.report.MonthlyReportResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.MonthlyWorkReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final MonthlyWorkReportService monthlyWorkReportService;
    private final CurrentUserService currentUserService;

    public MonthlyReportController(MonthlyWorkReportService monthlyWorkReportService,
                                   CurrentUserService currentUserService) {
        this.monthlyWorkReportService = monthlyWorkReportService;
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

    private Employee resolveCurrentEmployee() {
        return currentUserService.getCurrentEmployee()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Authenticated user has no employee profile"));
    }
}
