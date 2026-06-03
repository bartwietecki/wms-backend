package com.workforce.wms.service;

import com.workforce.wms.common.error.InvalidMonthlyReportException;
import com.workforce.wms.common.error.MonthlyReportNotFoundException;
import com.workforce.wms.dto.report.MonthlyReportDetailResponse;
import com.workforce.wms.dto.report.MonthlyReportPreviewResponse;
import com.workforce.wms.dto.report.MonthlyReportResponse;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.MonthlyWorkReport;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.repository.MonthlyWorkReportRepository;
import com.workforce.wms.repository.MonthlyWorkReportSpecifications;
import com.workforce.wms.repository.WorkEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MonthlyWorkReportService {

    private final MonthlyWorkReportRepository reportRepository;
    private final WorkEntryRepository workEntryRepository;

    public MonthlyWorkReportService(MonthlyWorkReportRepository reportRepository,
                                    WorkEntryRepository workEntryRepository) {
        this.reportRepository = reportRepository;
        this.workEntryRepository = workEntryRepository;
    }

    @Transactional(readOnly = true)
    public MonthlyReportPreviewResponse preview(Employee employee, int year, int month) {
        validateYearMonth(year, month);

        List<WorkEntry> entries = entriesForMonth(employee.getId(), year, month);
        int totalMinutes = sumMinutes(entries);
        int totalHours = totalMinutes / 60;

        MonthlyWorkReportStatus existingStatus = reportRepository
                .findByEmployeeIdAndYearAndMonth(employee.getId(), year, month)
                .map(MonthlyWorkReport::getStatus)
                .orElse(null);

        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        List<WorkEntryResponse> entryResponses = entries.stream()
                .map(we -> toWorkEntryResponse(we, employee))
                .toList();

        return new MonthlyReportPreviewResponse(
                employee.getId(),
                employeeName,
                year,
                month,
                totalMinutes,
                totalHours,
                entries.size(),
                entryResponses,
                existingStatus
        );
    }

    public MonthlyReportResponse submit(Employee employee, int year, int month) {
        validateYearMonth(year, month);

        List<WorkEntry> entries = entriesForMonth(employee.getId(), year, month);
        if (entries.isEmpty()) {
            throw new InvalidMonthlyReportException(
                    "Cannot submit report for " + year + "/" + month + ": no work entries found");
        }

        int totalMinutes = sumMinutes(entries);

        Optional<MonthlyWorkReport> existing =
                reportRepository.findByEmployeeIdAndYearAndMonth(employee.getId(), year, month);

        MonthlyWorkReport report;
        if (existing.isEmpty()) {
            report = new MonthlyWorkReport();
            report.setEmployee(employee);
            report.setYear(year);
            report.setMonth(month);
        } else {
            report = existing.get();
            if (report.getStatus() == MonthlyWorkReportStatus.SUBMITTED) {
                throw new InvalidMonthlyReportException(
                        "Report for " + year + "/" + month + " is already submitted");
            }
            if (report.getStatus() == MonthlyWorkReportStatus.APPROVED) {
                throw new InvalidMonthlyReportException(
                        "Report for " + year + "/" + month + " is already approved");
            }
            // REJECTED → allow resubmit: reset review fields
            report.setReviewedAt(null);
            report.setAdminComment(null);
        }

        report.setStatus(MonthlyWorkReportStatus.SUBMITTED);
        report.setTotalMinutes(totalMinutes);
        report.setSubmittedAt(OffsetDateTime.now());

        return toResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<MonthlyReportResponse> myReports(Employee employee) {
        return reportRepository.findAllByEmployeeIdOrderByYearDescMonthDesc(employee.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MonthlyReportResponse> findAllFiltered(MonthlyWorkReportStatus status, Long employeeId,
                                                       Integer year, Integer month, Pageable pageable) {
        Specification<MonthlyWorkReport> spec = Specification
                .where(MonthlyWorkReportSpecifications.hasStatus(status))
                .and(MonthlyWorkReportSpecifications.hasEmployeeId(employeeId))
                .and(MonthlyWorkReportSpecifications.hasYear(year))
                .and(MonthlyWorkReportSpecifications.hasMonth(month));

        return reportRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MonthlyReportDetailResponse getAdminReportDetail(Long id) {
        MonthlyWorkReport report = reportRepository.findById(id)
                .orElseThrow(() -> new MonthlyReportNotFoundException(id));

        Employee employee = report.getEmployee();
        List<WorkEntry> entries = entriesForMonth(employee.getId(), report.getYear(), report.getMonth());
        List<WorkEntryResponse> entryResponses = entries.stream()
                .map(we -> toWorkEntryResponse(we, employee))
                .toList();

        return toDetailResponse(report, entryResponses);
    }

    public MonthlyReportResponse approve(Long id) {
        MonthlyWorkReport report = reportRepository.findById(id)
                .orElseThrow(() -> new MonthlyReportNotFoundException(id));

        validateSubmitted(report);

        report.setStatus(MonthlyWorkReportStatus.APPROVED);
        report.setReviewedAt(OffsetDateTime.now());

        return toResponse(reportRepository.save(report));
    }

    public MonthlyReportResponse reject(Long id, String adminComment) {
        MonthlyWorkReport report = reportRepository.findById(id)
                .orElseThrow(() -> new MonthlyReportNotFoundException(id));

        validateSubmitted(report);

        report.setStatus(MonthlyWorkReportStatus.REJECTED);
        report.setReviewedAt(OffsetDateTime.now());
        report.setAdminComment(adminComment != null ? adminComment.trim() : null);

        return toResponse(reportRepository.save(report));
    }

    private void validateYearMonth(int year, int month) {
        if (year < 2000) {
            throw new InvalidMonthlyReportException("year must be >= 2000");
        }
        if (month < 1 || month > 12) {
            throw new InvalidMonthlyReportException("month must be between 1 and 12");
        }
    }

    private void validateSubmitted(MonthlyWorkReport report) {
        if (report.getStatus() != MonthlyWorkReportStatus.SUBMITTED) {
            throw new InvalidMonthlyReportException(
                    "Only SUBMITTED report can be changed. Current status: " + report.getStatus());
        }
    }

    private List<WorkEntry> entriesForMonth(Long employeeId, int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        return workEntryRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
                employeeId, monthStart, monthEnd);
    }

    private int sumMinutes(List<WorkEntry> entries) {
        return entries.stream().mapToInt(WorkEntry::getMinutes).sum();
    }

    private MonthlyReportResponse toResponse(MonthlyWorkReport report) {
        Employee employee = report.getEmployee();
        return new MonthlyReportResponse(
                report.getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                report.getYear(),
                report.getMonth(),
                report.getStatus(),
                report.getTotalMinutes(),
                report.getTotalMinutes() / 60,
                report.getSubmittedAt(),
                report.getReviewedAt(),
                report.getAdminComment()
        );
    }

    private MonthlyReportDetailResponse toDetailResponse(MonthlyWorkReport report,
                                                         List<WorkEntryResponse> entries) {
        Employee employee = report.getEmployee();
        return new MonthlyReportDetailResponse(
                report.getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                report.getYear(),
                report.getMonth(),
                report.getStatus(),
                report.getTotalMinutes(),
                report.getTotalMinutes() / 60,
                report.getSubmittedAt(),
                report.getReviewedAt(),
                report.getAdminComment(),
                entries
        );
    }

    private WorkEntryResponse toWorkEntryResponse(WorkEntry workEntry, Employee employee) {
        return new WorkEntryResponse(
                workEntry.getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                workEntry.getWorkDate(),
                workEntry.getMinutes(),
                workEntry.getDescription(),
                workEntry.getStatus()
        );
    }
}
