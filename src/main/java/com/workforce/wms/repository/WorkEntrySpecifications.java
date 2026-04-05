package com.workforce.wms.repository;

import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class WorkEntrySpecifications {

    private WorkEntrySpecifications() {}

    public static Specification<WorkEntry> hasStatus(WorkEntryStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<WorkEntry> hasEmployeeId(Long employeeId) {
        return (root, query, cb) ->
                employeeId == null ? null : cb.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<WorkEntry> workDateFrom(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("workDate"), from);
    }

    public static Specification<WorkEntry> workDateTo(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("workDate"), to);
    }
}
