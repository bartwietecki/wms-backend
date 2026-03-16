package com.workforce.wms.service;

import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.common.error.WorkEntryNotFoundException;
import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkEntryServiceTest {

    @Mock
    private WorkEntryRepository workEntryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private WorkEntryService workEntryService;

    @Captor
    private ArgumentCaptor<WorkEntry> workEntryCaptor;

    private Employee employee;
    private WorkEntry pendingWorkEntry;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@gmail.com");

        pendingWorkEntry = new WorkEntry();
        pendingWorkEntry.setId(10L);
        pendingWorkEntry.setEmployee(employee);
        pendingWorkEntry.setWorkDate(LocalDate.of(2026, 3, 13));
        pendingWorkEntry.setMinutes(120);
        pendingWorkEntry.setDescription("Worked on WMS Project");
        pendingWorkEntry.setStatus(WorkEntryStatus.PENDING);
    }

    @Test
    void create_shouldMapFieldsAndSetPendingStatus() {
        CreateWorkEntryRequest request = new CreateWorkEntryRequest(
                LocalDate.of(2026, 3, 13),
                120,
                "Worked on WMS Project"
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workEntryRepository.save(any(WorkEntry.class))).thenReturn(pendingWorkEntry);

        var response = workEntryService.create(1L, request);

        verify(workEntryRepository).save(workEntryCaptor.capture());
        WorkEntry toSave = workEntryCaptor.getValue();

        assertThat(toSave.getEmployee()).isEqualTo(employee);
        assertThat(toSave.getWorkDate()).isEqualTo(LocalDate.of(2026, 3, 13));
        assertThat(toSave.getMinutes()).isEqualTo(120);
        assertThat(toSave.getDescription()).isEqualTo("Worked on WMS Project");
        assertThat(toSave.getStatus()).isEqualTo(WorkEntryStatus.PENDING);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(WorkEntryStatus.PENDING);
        assertThat(response.minutes()).isEqualTo(120);
    }

    @Test
    void create_whenMinutesIsLessOrEqualZero_shouldThrow() {
        CreateWorkEntryRequest request = new CreateWorkEntryRequest(
                LocalDate.of(2026, 3, 13),
                0,
                "Worked on WMS Project"
        );

        assertThatThrownBy(() -> workEntryService.create(1L, request))
                .isInstanceOf(InvalidWorkEntryException.class)
                .hasMessage("minutes must be > 0");

        verify(employeeRepository, never()).findById(anyLong());
        verify(workEntryRepository, never()).save(any(WorkEntry.class));
    }

    @Test
    void myEntries_whenValidRange_shouldReturnMappedResponses() {
        when(workEntryRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
                1L,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
                )).thenReturn(List.of(pendingWorkEntry));

        var result = workEntryService.myEntries(
                1L,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.getFirst().status()).isEqualTo(WorkEntryStatus.PENDING);
        assertThat(result.getFirst().minutes()).isEqualTo(120);
        assertThat(result.getFirst().description()).isEqualTo("Worked on WMS Project");
    }

    @Test
    void approve_whenStatusIsPending_shouldSetApproved() {
        when(workEntryRepository.findById(10L)).thenReturn(Optional.of(pendingWorkEntry));
        when(workEntryRepository.save(any(WorkEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = workEntryService.approve(10L);

        verify(workEntryRepository).save(workEntryCaptor.capture());
        WorkEntry saved = workEntryCaptor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WorkEntryStatus.APPROVED);
        assertThat(response.status()).isEqualTo(WorkEntryStatus.APPROVED);
    }

    @Test
    void reject_whenStatusIsPending_shouldSetRejected() {
        when(workEntryRepository.findById(10L)).thenReturn(Optional.of(pendingWorkEntry));
        when(workEntryRepository.save(any(WorkEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = workEntryService.reject(10L);

        verify(workEntryRepository).save(workEntryCaptor.capture());
        WorkEntry saved = workEntryCaptor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WorkEntryStatus.REJECTED);
        assertThat(response.status()).isEqualTo(WorkEntryStatus.REJECTED);
    }

    @Test
    void approve_whenStatusIsNotPending_shouldThrow() {
        pendingWorkEntry.setStatus(WorkEntryStatus.APPROVED);
        when(workEntryRepository.findById(10L)).thenReturn(Optional.of(pendingWorkEntry));

        assertThatThrownBy(() -> workEntryService.approve(10L))
                .isInstanceOf(InvalidWorkEntryException.class)
                .hasMessage("Only PENDING work entry can be changed. Current status: APPROVED");

        verify(workEntryRepository, never()).save(any(WorkEntry.class));
    }

    @Test
    void reject_whenStatusIsNotPending_shouldThrow() {
        pendingWorkEntry.setStatus(WorkEntryStatus.REJECTED);
        when(workEntryRepository.findById(10L)).thenReturn(Optional.of(pendingWorkEntry));

        assertThatThrownBy(() -> workEntryService.reject(10L))
                .isInstanceOf(InvalidWorkEntryException.class)
                .hasMessage("Only PENDING work entry can be changed. Current status: REJECTED");

        verify(workEntryRepository, never()).save(any(WorkEntry.class));
    }

    @Test
    void approve_whenWorkEntryDoesNotExist_shouldThrow() {
        when(workEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workEntryService.approve(999L))
                .isInstanceOf(WorkEntryNotFoundException.class);
    }

    @Test
    void reject_whenWorkEntryDoesNotExist_shouldThrow() {
        when(workEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workEntryService.reject(999L))
                .isInstanceOf(WorkEntryNotFoundException.class);
    }
}