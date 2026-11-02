package edu.dvc.admin;

import edu.dvc.coordination.AssignmentCreateRequest;
import edu.dvc.coordination.AssignmentDto;
import edu.dvc.coordination.EmergencyCoordinationService;
import edu.dvc.coordination.EmergencyRequestDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminEmergencyRequestController {
    private final EmergencyCoordinationService service;

    public AdminEmergencyRequestController(EmergencyCoordinationService service) { this.service = service; }

    @GetMapping
    public List<EmergencyRequestDto> all() { return service.allRequests(); }

    @PostMapping("/{requestId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentDto assign(@PathVariable Long requestId, @Valid @RequestBody AssignmentCreateRequest request) {
        return service.assign(requestId, request.volunteerId());
    }

}
