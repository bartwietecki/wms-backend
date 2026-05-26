package com.workforce.wms.api.employee;

import com.workforce.wms.common.error.WorkEntryAccessDeniedException;
import com.workforce.wms.common.error.WorkEntryNotFoundException;
import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.WorkEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkEntryControllerTest {

    @Mock
    WorkEntryService workEntryService;

    @Mock
    CurrentUserService currentUserService;

    @Captor
    ArgumentCaptor<UpdateWorkEntryRequest> updateCaptor;

    private WorkEntryController controller;
    private Employee employee;

    private WorkEntryResponse sampleResponse() {
        return new WorkEntryResponse(
                10L, 1L, "John Doe",
                LocalDate.of(2026, 3, 13), 120,
                "Worked on WMS", WorkEntryStatus.PENDING
        );
    }

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        controller = new WorkEntryController(workEntryService, currentUserService);
        when(currentUserService.getCurrentEmployee()).thenReturn(Optional.of(employee));
    }

    @Test
    void update_shouldDelegateToServiceAndReturnResponse() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(employee), eq(10L), any(UpdateWorkEntryRequest.class)))
                .thenReturn(sampleResponse());

        var result = controller.update(10L, request);

        assertThat(result.id()).isEqualTo(10L);
        verify(workEntryService).updateOwnEntry(eq(employee), eq(10L), updateCaptor.capture());
        assertThat(updateCaptor.getValue()).isEqualTo(request);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void update_whenNotOwner_shouldPropagateAccessDenied() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(employee), eq(10L), any()))
                .thenThrow(new WorkEntryAccessDeniedException(10L));

        assertThatThrownBy(() -> controller.update(10L, request))
                .isInstanceOf(WorkEntryAccessDeniedException.class);
    }

    @Test
    void update_whenNotPending_shouldPropagateInvalidWorkEntry() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(employee), eq(10L), any()))
                .thenThrow(new InvalidWorkEntryException("Only PENDING work entry can be changed. Current status: APPROVED"));

        assertThatThrownBy(() -> controller.update(10L, request))
                .isInstanceOf(InvalidWorkEntryException.class);
    }

    @Test
    void update_whenNotFound_shouldPropagateNotFound() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(employee), eq(999L), any()))
                .thenThrow(new WorkEntryNotFoundException(999L));

        assertThatThrownBy(() -> controller.update(999L, request))
                .isInstanceOf(WorkEntryNotFoundException.class);
    }

    @Test
    void update_whenNoEmployeeProfile_shouldThrowForbidden() {
        when(currentUserService.getCurrentEmployee()).thenReturn(Optional.empty());
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );

        assertThatThrownBy(() -> controller.update(10L, request))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(workEntryService);
    }

    @Test
    void delete_shouldDelegateToService() {
        doNothing().when(workEntryService).deleteOwnEntry(employee, 10L);

        controller.delete(10L);

        verify(workEntryService).deleteOwnEntry(employee, 10L);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void delete_whenNotOwner_shouldPropagateAccessDenied() {
        doThrow(new WorkEntryAccessDeniedException(10L))
                .when(workEntryService).deleteOwnEntry(employee, 10L);

        assertThatThrownBy(() -> controller.delete(10L))
                .isInstanceOf(WorkEntryAccessDeniedException.class);
    }

    @Test
    void delete_whenNoEmployeeProfile_shouldThrowForbidden() {
        when(currentUserService.getCurrentEmployee()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(10L))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(workEntryService);
    }
}
