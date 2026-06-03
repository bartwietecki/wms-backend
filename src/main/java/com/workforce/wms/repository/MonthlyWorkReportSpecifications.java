package com.workforce.wms.repository;

import com.workforce.wms.entity.MonthlyWorkReport;
import com.workforce.wms.entity.MonthlyWorkReportStatus;
import org.springframework.data.jpa.domain.Specification;

public class MonthlyWorkReportSpecifications {

    private MonthlyWorkReportSpecifications() {}

    public static Specification<MonthlyWorkReport> hasStatus(MonthlyWorkReportStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<MonthlyWorkReport> hasEmployeeId(Long employeeId) {
        return (root, query, cb) ->
                employeeId == null ? null : cb.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<MonthlyWorkReport> hasYear(Integer year) {
        return (root, query, cb) ->
                year == null ? null : cb.equal(root.get("year"), year);
    }

    public static Specification<MonthlyWorkReport> hasMonth(Integer month) {
        return (root, query, cb) ->
                month == null ? null : cb.equal(root.get("month"), month);
    }
}
