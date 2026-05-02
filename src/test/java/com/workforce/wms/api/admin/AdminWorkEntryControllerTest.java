package com.workforce.wms.api.admin;

import com.workforce.wms.dto.workentry.RejectWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.service.WorkEntryService;
import com.workforce.wms.service.WorkEntryStatusHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminWorkEntryControllerTest {

    @Mock
    WorkEntryService workEntryService;

    @Mock
    WorkEntryStatusHistoryService historyService;

    @Captor
    ArgumentCaptor<UpdateWorkEntryRequest> updateCaptor;

    private AdminWorkEntryController controller;

    private WorkEntryResponse sampleResponse() {
        return new WorkEntryResponse(
                10L, 1L, "John Doe",
                LocalDate.of(2026, 3, 13), 120,
                "Worked on WMS", WorkEntryStatus.PENDING
        );
    }

    @BeforeEach
    void setUp() {
        controller = new AdminWorkEntryController(workEntryService, historyService);
    }

    @Test
    void getAll_shouldDelegateToServiceAndReturnPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<WorkEntryResponse> page = new PageImpl<>(List.of(sampleResponse()));

        when(workEntryService.findAllFiltered(null, null, null, null, pageable)).thenReturn(page);

        var result = controller.getAll(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(10L);
        assertThat(result.getContent().getFirst().employeeName()).isEqualTo("John Doe");
        verify(workEntryService).findAllFiltered(null, null, null, null, pageable);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void getAll_withFilters_shouldPassThemToService() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);
        Page<WorkEntryResponse> page = new PageImpl<>(List.of(sampleResponse()));

        when(workEntryService.findAllFiltered(WorkEntryStatus.PENDING, 1L, from, to, pageable))
                .thenReturn(page);

        var result = controller.getAll(WorkEntryStatus.PENDING, 1L, from, to, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(workEntryService).findAllFiltered(WorkEntryStatus.PENDING, 1L, from, to, pageable);
    }

    @Test
    void approve_shouldDelegateToServiceAndReturnResponse() {
        WorkEntryResponse approved = new WorkEntryResponse(
                10L, 1L, "John Doe",
                LocalDate.of(2026, 3, 13), 120,
                "Worked on WMS", WorkEntryStatus.APPROVED
        );
        when(workEntryService.approve(10L)).thenReturn(approved);

        var result = controller.approve(10L);

        assertThat(result.status()).isEqualTo(WorkEntryStatus.APPROVED);
        verify(workEntryService).approve(10L);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void reject_shouldDelegateToServiceAndReturnResponse() {
        WorkEntryResponse rejected = new WorkEntryResponse(
                10L, 1L, "John Doe",
                LocalDate.of(2026, 3, 13), 120,
                "Worked on WMS", WorkEntryStatus.REJECTED
        );
        RejectWorkEntryRequest request = new RejectWorkEntryRequest("Description is too vague");
        when(workEntryService.reject(10L, "Description is too vague")).thenReturn(rejected);

        var result = controller.reject(10L, request);

        assertThat(result.status()).isEqualTo(WorkEntryStatus.REJECTED);
        verify(workEntryService).reject(10L, "Description is too vague");
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void update_shouldDelegateToServiceAndReturnResponse() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 3, 13), 120, "Updated"
        );
        when(workEntryService.update(eq(10L), any(UpdateWorkEntryRequest.class)))
                .thenReturn(sampleResponse());

        var result = controller.update(10L, request);

        assertThat(result.id()).isEqualTo(10L);
        verify(workEntryService).update(eq(10L), updateCaptor.capture());
        assertThat(updateCaptor.getValue()).isEqualTo(request);
        verifyNoMoreInteractions(workEntryService);
    }

    @Test
    void delete_shouldDelegateToService() {
        doNothing().when(workEntryService).delete(10L);

        controller.delete(10L);

        verify(workEntryService).delete(10L);
        verifyNoMoreInteractions(workEntryService);
    }
}
