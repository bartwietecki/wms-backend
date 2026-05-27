package com.workforce.wms.service;

import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.User;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public CurrentUserService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Returns the User entity matching the currently authenticated username.
     * Returns empty if no matching User exists in the database.
     */
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(auth.getName());
    }

    /**
     * Returns the Employee linked to the currently authenticated user.
     * Returns empty if the principal has no matching User or no linked Employee row.
     */
    public Optional<Employee> getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return employeeRepository.findByUser_Username(auth.getName());
    }
}