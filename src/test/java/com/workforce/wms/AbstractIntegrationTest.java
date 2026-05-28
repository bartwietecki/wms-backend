package com.workforce.wms;

import com.workforce.wms.entity.Employee;
import com.workforce.wms.entity.User;
import com.workforce.wms.entity.WorkEntry;
import com.workforce.wms.entity.WorkEntryStatus;
import com.workforce.wms.repository.EmployeeRepository;
import com.workforce.wms.repository.UserRepository;
import com.workforce.wms.repository.WorkEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

/**
 * Base class for integration tests which boots the full Spring context
 * against a Testcontainers PostgreSQL instance.
 * Each test method runs inside a transaction that is rolled back on completion,
 * giving per-test isolation without any cleanup SQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class AbstractIntegrationTest {

    // Credentials matching src/test/resources/application.yaml
    protected static final String ADMIN      = "admin";
    protected static final String ADMIN_PASS = "test-password";
    protected static final String JAN        = "jan.kowalski";
    protected static final String ANNA       = "anna.nowak";
    protected static final String EMP_PASS   = "test-password";

    // Shared infrastructure
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkEntryRepository workEntryRepository;

    protected MockMvc mockMvc;

    // IDs set in @BeforeEach, captured after save
    protected Long janPendingEntryId;
    protected Long janApprovedEntryId;
    protected Long annaPendingEntryId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        User janUser = persistUser("jan.kowalski", "jan@test.local");
        Employee jan = persistEmployee("Jan", "Kowalski", "jan@test.local", janUser);
        janPendingEntryId  = persistWorkEntry(jan, WorkEntryStatus.PENDING).getId();
        janApprovedEntryId = persistWorkEntry(jan, WorkEntryStatus.APPROVED).getId();

        User annaUser = persistUser("anna.nowak", "anna@test.local");
        Employee anna = persistEmployee("Anna", "Nowak", "anna@test.local", annaUser);
        annaPendingEntryId = persistWorkEntry(anna, WorkEntryStatus.PENDING).getId();
    }

    // Helpers
    private User persistUser(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setAuthProvider("LOCAL");
        u.setEnabled(true);
        return userRepository.save(u);
    }

    private Employee persistEmployee(String firstName, String lastName, String email, User user) {
        Employee e = new Employee();
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setEmail(email);
        e.setEmploymentType("FULL_TIME");
        e.setActive(true);
        e.setUser(user);
        return employeeRepository.save(e);
    }

    private WorkEntry persistWorkEntry(Employee employee, WorkEntryStatus status) {
        WorkEntry we = new WorkEntry();
        we.setEmployee(employee);
        we.setWorkDate(LocalDate.of(2026, 3, 1));
        we.setMinutes(480);
        we.setDescription("Test entry");
        we.setStatus(status);
        return workEntryRepository.save(we);
    }
}
