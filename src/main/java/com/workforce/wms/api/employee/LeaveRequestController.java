package com.workforce.wms.api.employee;

import com.workforce.wms.dto.leaverequest.CreateLeaveRequestRequest;
import com.workforce.wms.dto.leaverequest.LeaveRequestResponse;
import com.workforce.wms.entity.Employee;
import com.workforce.wms.service.CurrentUserService;
import com.workforce.wms.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final CurrentUserService currentUserService;

    public LeaveRequestController(LeaveRequestService leaveRequestService, CurrentUserService currentUserService) {
        this.leaveRequestService = leaveRequestService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse create(@Valid @RequestBody CreateLeaveRequestRequest request) {
        return leaveRequestService.create(resolveCurrentEmployee(), request);
    }

    @GetMapping("/my")
    public List<LeaveRequestResponse> getMyRequests() {
        return leaveRequestService.myRequests(resolveCurrentEmployee());
    }

    private Employee resolveCurrentEmployee() {
        return currentUserService.getCurrentEmployee()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Authenticated user has no employee profile"));
    }
}
