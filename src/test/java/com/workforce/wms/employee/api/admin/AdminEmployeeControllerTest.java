package com.workforce.wms.employee.api.admin;

import com.workforce.wms.api.admin.AdminEmployeeController;
import com.workforce.wms.dto.employee.CreateEmployeeRequest;
import com.workforce.wms.dto.employee.EmployeeResponse;
import com.workforce.wms.dto.employee.UpdateEmployeeRequest;
import com.workforce.wms.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEmployeeControllerTest {

    @Mock
    EmployeeService employeeService;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    ArgumentCaptor<CreateEmployeeRequest> createCaptor;

    @Captor
    ArgumentCaptor<UpdateEmployeeRequest> updateCaptor;

    private AdminEmployeeController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminEmployeeController(employeeService);
    }

    @Test
    void listEmployees_shouldDelegateToService_andReturnResult() {
        // given
        EmployeeResponse employee = new EmployeeResponse(
                2L, "John", "Doe", "johndoe@gmail.com", "Developer", "B2B", true
        );

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());
        Page<EmployeeResponse> page = new PageImpl<>(List.of(employee), pageable, 1);

        when(employeeService.findAll(eq(true), any(Pageable.class))).thenReturn(page);

        // when
        Page<EmployeeResponse> result = controller.listEmployees(true, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(2L);

        verify(employeeService).findAll(eq(true), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    void create_shouldDelegateToService_andReturnResponse() {
        // given
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Adam", "Smith", "adamsmith@gmail.com", "Developer", "Mandate contract"
        );

        EmployeeResponse response = new EmployeeResponse(
                10L, "Adam", "Smith", "adamsmith@gmail.com", "Developer", "Mandate contract", true
        );

        when(employeeService.create(any(CreateEmployeeRequest.class))).thenReturn(response);

        // when
        EmployeeResponse result = controller.create(request);

        // then
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.email()).isEqualTo("adamsmith@gmail.com");

        verify(employeeService).create(createCaptor.capture());
        assertThat(createCaptor.getValue()).isEqualTo(request);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    void getEmployee_shouldDelegateToService() {
        // given
        EmployeeResponse response = new EmployeeResponse(
                2L, "John", "Doe", "johndoe@gmail.com", "Developer", "B2B", true
        );

        when(employeeService.findById(2L)).thenReturn(response);

        // when
        EmployeeResponse result = controller.getEmployee(2L);

        // then
        assertThat(result.id()).isEqualTo(2L);

        verify(employeeService).findById(2L);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    void updateEmployee_shouldDelegateToService_andReturnResponse() {
        // given
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "John2", "Doe2", "Senior Developer", "B2B"
        );

        EmployeeResponse response = new EmployeeResponse(
                2L, "John2", "Doe2", "johndoe@gmail.com", "Senior Developer", "B2B", true
        );

        when(employeeService.update(eq(2L), any(UpdateEmployeeRequest.class))).thenReturn(response);

        // when
        EmployeeResponse result = controller.updateEmployee(2L, request);

        // then
        assertThat(result.firstName()).isEqualTo("John2");
        assertThat(result.email()).isEqualTo("johndoe@gmail.com");

        verify(employeeService).update(eq(2L), updateCaptor.capture());
        assertThat(updateCaptor.getValue()).isEqualTo(request);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    void activate_shouldDelegateToService() {
        // given
        EmployeeResponse response = new EmployeeResponse(
                2L, "John", "Doe", "johndoe@gmail.com", "Developer", "B2B", true
        );

        when(employeeService.activate(2L)).thenReturn(response);

        // when
        EmployeeResponse result = controller.activate(2L);

        // then
        assertThat(result.active()).isTrue();

        verify(employeeService).activate(2L);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    void deactivate_shouldDelegateToService() {
        // given
        EmployeeResponse response = new EmployeeResponse(
                2L, "John", "Doe", "johndoe@gmail.com", "Developer", "B2B", false
        );

        when(employeeService.deactivate(2L)).thenReturn(response);

        // when
        EmployeeResponse result = controller.deactivate(2L);

        // then
        assertThat(result.active()).isFalse();

        verify(employeeService).deactivate(2L);
        verifyNoMoreInteractions(employeeService);
    }

}