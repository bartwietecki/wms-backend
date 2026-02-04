package com.workforce.wms.employee.api.admin;

import com.workforce.wms.employee.api.dto.CreateEmployeeRequest;
import com.workforce.wms.employee.api.dto.EmployeeResponse;
import com.workforce.wms.employee.api.dto.UpdateEmployeeRequest;
import com.workforce.wms.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/employees")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Page<EmployeeResponse> listEmployees(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return employeeService.findAll(active, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.create(request);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployee(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(@PathVariable Long id,
                                           @Valid @RequestBody UpdateEmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public EmployeeResponse activate(@PathVariable Long id) {
        return employeeService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public EmployeeResponse deactivate(@PathVariable Long id) {
        return employeeService.deactivate(id);
    }

}
