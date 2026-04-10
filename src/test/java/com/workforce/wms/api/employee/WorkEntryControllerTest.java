package com.workforce.wms.api.employee;

import com.workforce.wms.common.error.WorkEntryAccessDeniedException;
import com.workforce.wms.common.error.WorkEntryNotFoundException;
import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.service.WorkEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkEntryControllerTest {

    @Mock
    WorkEntryService workEntryService;

    @Captor
    ArgumentCaptor<UpdateWorkEntryRequest> updateCaptor;

    private WorkEntryController controller;

    private WorkEntryResponse sampleResponse() {
        return new WorkEntryResponse(
                10L, 1L, "John Doe",
                LocalDate.of(2026, 3, 13), 120,
                "Worked on WMS", WorkEntryStatus.PENDING
        );
    }

    @BeforeEach
    void setUp() {
        controller = new WorkEntryController(workEntryService);
    }

    @Test
    void update_shouldDelegateToServiceAndReturnResponse() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(1L), eq(10L), any(UpdateWorkEntryRequest.class)))
                .thenReturn(sampleResponse());

        var result = controller.update(1L, 10L, request);

        assertThat(result.id()).isEqualTo(10L);
        verify(workEntryService).updateOwnEntry(eq(1L), eq(10L), updateCaptor.capture());
        assertThat(updateCaptor.getValue()).isEqualTo(request);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void update_whenNotOwner_shouldPropagateAccessDenied() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(99L), eq(10L), any()))
                .thenThrow(new WorkEntryAccessDeniedException(10L));

        assertThatThrownBy(() -> controller.update(99L, 10L, request))
                .isInstanceOf(WorkEntryAccessDeniedException.class);
    }

    @Test
    void update_whenNotPending_shouldPropagateInvalidWorkEntry() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(1L), eq(10L), any()))
                .thenThrow(new InvalidWorkEntryException("Only PENDING work entry can be changed. Current status: APPROVED"));

        assertThatThrownBy(() -> controller.update(1L, 10L, request))
                .isInstanceOf(InvalidWorkEntryException.class);
    }

    @Test
    void update_whenNotFound_shouldPropagateNotFound() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 90, "Updated"
        );
        when(workEntryService.updateOwnEntry(eq(1L), eq(999L), any()))
                .thenThrow(new WorkEntryNotFoundException(999L));

        assertThatThrownBy(() -> controller.update(1L, 999L, request))
                .isInstanceOf(WorkEntryNotFoundException.class);
    }

    @Test
    void delete_shouldDelegateToService() {
        doNothing().when(workEntryService).deleteOwnEntry(1L, 10L);

        controller.delete(1L, 10L);

        verify(workEntryService).deleteOwnEntry(1L, 10L);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void delete_whenNotOwner_shouldPropagateAccessDenied() {
        doThrow(new WorkEntryAccessDeniedException(10L))
                .when(workEntryService).deleteOwnEntry(99L, 10L);

        assertThatThrownBy(() -> controller.delete(99L, 10L))
                .isInstanceOf(WorkEntryAccessDeniedException.class);
    }
}
