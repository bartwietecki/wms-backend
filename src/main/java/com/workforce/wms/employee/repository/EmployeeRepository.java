package com.workforce.wms.employee.repository;

import com.workforce.wms.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);

    Page<Employee> findAllByActive(boolean active, Pageable pageable);
}
