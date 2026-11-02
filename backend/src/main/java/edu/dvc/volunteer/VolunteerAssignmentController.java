package edu.dvc.volunteer;

import edu.dvc.coordination.AssignmentDto;
import edu.dvc.coordination.EmergencyCoordinationService;
import edu.dvc.coordination.EmergencyRequestDto;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/volunteer/me/assignments")
public class VolunteerAssignmentController {
    private final EmergencyCoordinationService service;

    public VolunteerAssignmentController(EmergencyCoordinationService service) { this.service = service; }

    @GetMapping
    public List<EmergencyRequestDto> mine(Principal principal) { return service.volunteerAssignments(principal.getName()); }
}
