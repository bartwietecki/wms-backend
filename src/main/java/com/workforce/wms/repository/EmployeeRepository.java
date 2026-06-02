package com.workforce.wms.repository;

import com.workforce.wms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);

    long countByActive(boolean active);

    Page<Employee> findAllByActive(boolean active, Pageable pageable);

    Optional<Employee> findByUser_Username(String username);
}
