package com.workforce.wms.common.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void handleEmployeeNotFound_shouldReturn404WithCorrectBody() {
        var ex = new EmployeeNotFoundException(42L);

        ResponseEntity<ApiError> response = handler.handleEmployeeNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("EMPLOYEE_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Employee not found: 42");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void handleEmailAlreadyExists_shouldReturn409WithCorrectBody() {
        var ex = new EmailAlreadyExistsException("john@example.com");

        ResponseEntity<ApiError> response = handler.handleEmailAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("EMPLOYEE_EMAIL_EXISTS");
        assertThat(response.getBody().message()).contains("john@example.com");
    }

    @Test
    void handleInvalidWorkEntry_shouldReturn400WithCorrectBody() {
        var ex = new InvalidWorkEntryException("from must be <= to");

        ResponseEntity<ApiError> response = handler.handleInvalidWorkEntry(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_WORK_ENTRY");
        assertThat(response.getBody().message()).isEqualTo("from must be <= to");
    }

    @Test
    void handleWorkEntryNotFound_shouldReturn404WithCorrectBody() {
        var ex = new WorkEntryNotFoundException(99L);

        ResponseEntity<ApiError> response = handler.handleWorkEntryNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("WORK_ENTRY_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("WorkEntry not found: 99");
    }

    @Test
    void handleValidation_shouldReturn400WithFieldDetails() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        bindingResult.addError(new FieldError("request", "firstName", "must not be blank"));

        var ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details()).hasSize(2);
        assertThat(response.getBody().details())
                .anyMatch(d -> d.field().equals("email") && d.message().equals("must not be blank"));
    }
}
