package com.workforce.wms.service;

import com.workforce.wms.common.error.InvalidWorkEntryException;
import com.workforce.wms.common.error.WorkEntryNotFoundException;
import com.workforce.wms.dto.workentry.CreateWorkEntryRequest;
import com.workforce.wms.dto.workentry.UpdateWorkEntryRequest;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
        assertThat(response.employeeId()).isEqualTo(1L);
        assertThat(response.employeeName()).isEqualTo("John Doe");
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
    void myEntries_whenFromIsAfterTo_shouldThrow() {
        assertThatThrownBy(() -> workEntryService.myEntries(
                1L,
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 3, 1)))
                .isInstanceOf(InvalidWorkEntryException.class)
                .hasMessage("from must be <= to");

        verify(workEntryRepository, never())
                .findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(any(), any(), any());
    }

    @Test
    void create_whenEmployeeNotFound_shouldThrow() {
        CreateWorkEntryRequest request = new CreateWorkEntryRequest(
                LocalDate.of(2026, 3, 13), 120, "desc"
        );

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workEntryService.create(99L, request))
                .isInstanceOf(com.workforce.wms.common.error.EmployeeNotFoundException.class);

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
        assertThat(result.getFirst().employeeId()).isEqualTo(1L);
        assertThat(result.getFirst().employeeName()).isEqualTo("John Doe");
        assertThat(result.getFirst().status()).isEqualTo(WorkEntryStatus.PENDING);
        assertThat(result.getFirst().minutes()).isEqualTo(120);
        assertThat(result.getFirst().description()).isEqualTo("Worked on WMS Project");
    }

    @Test
    void findAllFiltered_whenFromIsAfterTo_shouldThrow() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate from = LocalDate.of(2026, 4, 30);
        LocalDate to = LocalDate.of(2026, 4, 1);

        assertThatThrownBy(() -> workEntryService.findAllFiltered(
                null, null, from, to, pageable))
                .isInstanceOf(InvalidWorkEntryException.class)
                .hasMessage("from must be <= to");

        verify(workEntryRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAllFiltered_shouldReturnPagedAndMappedResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        when(workEntryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(pendingWorkEntry)));

        var result = workEntryService.findAllFiltered(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(10L);
        assertThat(result.getContent().getFirst().employeeId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().employeeName()).isEqualTo("John Doe");
        assertThat(result.getContent().getFirst().status()).isEqualTo(WorkEntryStatus.PENDING);
        verify(workEntryRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAllFiltered_whenNoEntries_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(workEntryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = workEntryService.findAllFiltered(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(workEntryRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void update_shouldUpdateFieldsAndReturnResponse() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1),
                90,
                "Updated description"
        );

        when(workEntryRepository.findById(10L)).thenReturn(Optional.of(pendingWorkEntry));
        when(workEntryRepository.save(any(WorkEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = workEntryService.update(10L, request);

        verify(workEntryRepository).save(workEntryCaptor.capture());
        WorkEntry saved = workEntryCaptor.getValue();

        assertThat(saved.getWorkDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(saved.getMinutes()).isEqualTo(90);
        assertThat(saved.getDescription()).isEqualTo("Updated description");

        assertThat(response.workDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(response.minutes()).isEqualTo(90);
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.employeeId()).isEqualTo(1L);
    }

    @Test
    void update_whenWorkEntryDoesNotExist_shouldThrow() {
        UpdateWorkEntryRequest request = new UpdateWorkEntryRequest(
                LocalDate.of(2026, 4, 1), 60, null
        );

        when(workEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workEntryService.update(999L, request))
                .isInstanceOf(WorkEntryNotFoundException.class);

        verify(workEntryRepository, never()).save(any(WorkEntry.class));
    }

    @Test
    void delete_shouldDeleteWorkEntry() {
        when(workEntryRepository.existsById(10L)).thenReturn(true);

        workEntryService.delete(10L);

        verify(workEntryRepository).deleteById(10L);
    }

    @Test
    void delete_whenWorkEntryDoesNotExist_shouldThrow() {
        when(workEntryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> workEntryService.delete(999L))
                .isInstanceOf(WorkEntryNotFoundException.class);

        verify(workEntryRepository, never()).deleteById(anyLong());
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
        assertThat(response.employeeId()).isEqualTo(1L);
        assertThat(response.employeeName()).isEqualTo("John Doe");
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
        assertThat(response.employeeId()).isEqualTo(1L);
        assertThat(response.employeeName()).isEqualTo("John Doe");
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