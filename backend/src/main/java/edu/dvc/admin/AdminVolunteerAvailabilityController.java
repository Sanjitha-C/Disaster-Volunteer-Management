package edu.dvc.admin;

import edu.dvc.volunteer.VolunteerAvailabilityDto;
import edu.dvc.volunteer.VolunteerManagementService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/volunteers")
public class AdminVolunteerAvailabilityController {
    private final VolunteerManagementService volunteerService;

    public AdminVolunteerAvailabilityController(VolunteerManagementService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping("/availability")
    public List<VolunteerAvailabilityDto> availability() {
        return volunteerService.listVolunteerAvailability();
    }
}
