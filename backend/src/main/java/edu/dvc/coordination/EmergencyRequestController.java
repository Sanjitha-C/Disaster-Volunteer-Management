package edu.dvc.coordination;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/requests")
public class EmergencyRequestController {
    private final EmergencyCoordinationService service;

    public EmergencyRequestController(EmergencyCoordinationService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmergencyRequestDto create(Principal principal, @Valid @RequestBody EmergencyRequestCreateRequest request) {
        return service.createRequest(principal.getName(), request);
    }

    @GetMapping
    public List<EmergencyRequestDto> mine(Principal principal) { return service.myRequests(principal.getName()); }
}
