package com.workforce.wms.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ApiError body = new ApiError(
                "EMPLOYEE_EMAIL_EXISTS",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        ApiError body = new ApiError(
                "VALIDATION_ERROR",
                "Validation failed",
                details
        );
        return ResponseEntity.badRequest().body(body);
    }

    private ApiError.FieldError toFieldError(FieldError fe) {
        return new ApiError.FieldError(fe.getField(), fe.getDefaultMessage());
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiError> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ApiError body = new ApiError(
                "EMPLOYEE_NOT_FOUND",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidWorkEntryException.class)
    public ResponseEntity<ApiError> handleInvalidWorkEntry(InvalidWorkEntryException ex) {
        ApiError body = new ApiError(
                "INVALID_WORK_ENTRY",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(WorkEntryNotFoundException.class)
    public ResponseEntity<ApiError> handleWorkEntryNotFound(WorkEntryNotFoundException ex) {
        ApiError body = new ApiError(
                "WORK_ENTRY_NOT_FOUND",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(WorkEntryAccessDeniedException.class)
    public ResponseEntity<ApiError> handleWorkEntryAccessDenied(WorkEntryAccessDeniedException ex) {
        ApiError body = new ApiError(
                "WORK_ENTRY_ACCESS_DENIED",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<ApiError> handleLeaveRequestNotFound(LeaveRequestNotFoundException ex) {
        ApiError body = new ApiError(
                "LEAVE_REQUEST_NOT_FOUND",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidLeaveRequestException.class)
    public ResponseEntity<ApiError> handleInvalidLeaveRequest(InvalidLeaveRequestException ex) {
        ApiError body = new ApiError(
                "INVALID_LEAVE_REQUEST",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MonthlyReportNotFoundException.class)
    public ResponseEntity<ApiError> handleMonthlyReportNotFound(MonthlyReportNotFoundException ex) {
        ApiError body = new ApiError(
                "MONTHLY_REPORT_NOT_FOUND",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidMonthlyReportException.class)
    public ResponseEntity<ApiError> handleInvalidMonthlyReport(InvalidMonthlyReportException ex) {
        ApiError body = new ApiError(
                "INVALID_MONTHLY_REPORT",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }
}
