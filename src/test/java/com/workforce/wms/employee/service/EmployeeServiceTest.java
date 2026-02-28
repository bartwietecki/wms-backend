package com.workforce.wms.employee.service;

import com.workforce.wms.common.error.EmailAlreadyExistsException;
import com.workforce.wms.common.error.EmployeeNotFoundException;
import com.workforce.wms.dto.employee.CreateEmployeeRequest;
import com.workforce.wms.dto.employee.UpdateEmployeeRequest;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Employee> employeeCaptor;

    private Employee existingEmployee;

    @BeforeEach
    void setUp() {
        existingEmployee = new Employee();
        existingEmployee.setId(2L);
        existingEmployee.setFirstName("John");
        existingEmployee.setLastName("Doe");
        existingEmployee.setEmail("johndoe@gmail.com");
        existingEmployee.setPosition("Developer");
        existingEmployee.setEmploymentType("B2B");
        existingEmployee.setActive(true);
    }

    @Test
    void findAll_whenActiveNull_shouldUseFindAll() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        Page<Employee> page = new PageImpl<>(java.util.List.of(existingEmployee), pageable, 1);

        when(employeeRepository.findAll(pageable)).thenReturn(page);

        var result = employeeService.findAll(null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(2L);

        verify(employeeRepository).findAll(pageable);
        verify(employeeRepository, never()).findAllByActive(anyBoolean(), any());
    }

    @Test
    void findAll_whenActiveProvided_shouldUseFindAllByActive() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> page = new PageImpl<>(java.util.List.of(existingEmployee), pageable, 1);

        when(employeeRepository.findAllByActive(true, pageable)).thenReturn(page);

        var result = employeeService.findAll(true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(employeeRepository).findAllByActive(true, pageable);
        verify(employeeRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void create_whenEmailExists_shouldThrow() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "John", "Doe", "johndoe@gmail.com", "Developer", "B2B"
        );

        when(employeeRepository.existsByEmail("johndoe@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void create_shouldMapFieldsAndSetActiveTrue() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Adam", "Smith", "adamsmith@gmail.com", "Developer", "Mandate contract"
        );

        when(employeeRepository.existsByEmail("adamsmith@gmail.com")).thenReturn(false);

        Employee savedEmployee = new Employee();
        savedEmployee.setId(10L);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        var response = employeeService.create(request);

        verify(employeeRepository).save(employeeCaptor.capture());
        Employee toSave = employeeCaptor.getValue();

        assertThat(toSave.getFirstName()).isEqualTo("Adam");
        assertThat(toSave.getLastName()).isEqualTo("Smith");
        assertThat(toSave.getEmail()).isEqualTo("adamsmith@gmail.com");
        assertThat(toSave.getPosition()).isEqualTo("Developer");
        assertThat(toSave.getEmploymentType()).isEqualTo("Mandate contract");
        assertThat(toSave.isActive()).isTrue();

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(999L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void update_shouldUpdateFields_andKeepEmail() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "John2", "Doe2", "Senior Developer", "B2B"
        );

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = employeeService.update(2L, request);

        verify(employeeRepository).save(employeeCaptor.capture());
        Employee saved = employeeCaptor.getValue();

        assertThat(saved.getFirstName()).isEqualTo("John2");
        assertThat(saved.getLastName()).isEqualTo("Doe2");
        assertThat(saved.getPosition()).isEqualTo("Senior Developer");
        assertThat(saved.getEmploymentType()).isEqualTo("B2B");
        assertThat(saved.getEmail()).isEqualTo("johndoe@gmail.com"); // keep unchanged

        assertThat(response.firstName()).isEqualTo("John2");
        assertThat(response.lastName()).isEqualTo("Doe2");
        assertThat(response.email()).isEqualTo("johndoe@gmail.com");
    }

    @Test
    void activate_shouldSetActiveTrue() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(existingEmployee));

        var response = employeeService.activate(2L);

        assertThat(response.active()).isTrue();
    }

    @Test
    void deactivate_shouldSetActiveFalse() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(existingEmployee));

        var response = employeeService.deactivate(2L);

        assertThat(response.active()).isFalse();
    }
}