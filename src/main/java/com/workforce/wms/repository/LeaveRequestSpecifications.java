package com.workforce.wms.repository;

import com.workforce.wms.entity.LeaveRequest;
import com.workforce.wms.entity.LeaveRequestStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class LeaveRequestSpecifications {

    private LeaveRequestSpecifications() {}

    public static Specification<LeaveRequest> hasStatus(LeaveRequestStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<LeaveRequest> hasEmployeeId(Long employeeId) {
        return (root, query, cb) ->
                employeeId == null ? null : cb.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<LeaveRequest> startDateFrom(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    public static Specification<LeaveRequest> startDateTo(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("startDate"), to);
    }
}
