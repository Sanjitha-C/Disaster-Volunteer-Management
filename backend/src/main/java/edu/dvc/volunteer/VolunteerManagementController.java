package edu.dvc.volunteer;

import edu.dvc.domain.AvailabilityStatus;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/volunteer/me")
public class VolunteerManagementController {
    private final VolunteerManagementService service;

    public VolunteerManagementController(VolunteerManagementService service) {
        this.service = service;
    }

    @GetMapping("/profile")
    public VolunteerProfileDto profile(Principal principal) {
        return service.getProfile(principal.getName());
    }

    @PutMapping("/profile")
    public VolunteerProfileDto updateProfile(Principal principal,
                                             @Valid @RequestBody UpdateVolunteerProfileRequest request) {
        return service.updateProfile(principal.getName(), request);
    }

    @GetMapping("/skills")
    public List<VolunteerSkillDto> skills(Principal principal) {
        return service.getSkills(principal.getName());
    }

    @PostMapping("/skills")
    public ResponseEntity<VolunteerSkillDto> addSkill(Principal principal, @Valid @RequestBody SkillRequest request) {
        VolunteerSkillDto created = service.addSkill(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/volunteer/me/skills/" + created.id())).body(created);
    }

    @PutMapping("/skills/{volunteerSkillId}")
    public VolunteerSkillDto updateSkill(Principal principal, @PathVariable Long volunteerSkillId,
                                         @Valid @RequestBody SkillRequest request) {
        return service.renameSkill(principal.getName(), volunteerSkillId, request);
    }

    @DeleteMapping("/skills/{volunteerSkillId}")
    public ResponseEntity<Void> removeSkill(Principal principal, @PathVariable Long volunteerSkillId) {
        service.removeSkill(principal.getName(), volunteerSkillId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    public AvailabilityResponse availability(Principal principal) {
        return service.getAvailability(principal.getName());
    }

    @PutMapping("/availability")
    public AvailabilityResponse updateAvailability(Principal principal,
                                                   @Valid @RequestBody AvailabilityStatusRequest request) {
        return service.updateAvailability(principal.getName(), request.status());
    }

    @PostMapping("/availability/slots")
    public ResponseEntity<AvailabilitySlotDto> addSlot(Principal principal,
                                                       @Valid @RequestBody AvailabilitySlotRequest request) {
        AvailabilitySlotDto created = service.addAvailabilitySlot(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/volunteer/me/availability/slots/" + created.id())).body(created);
    }

    @PutMapping("/availability/slots/{slotId}")
    public AvailabilitySlotDto updateSlot(Principal principal, @PathVariable Long slotId,
                                          @Valid @RequestBody AvailabilitySlotRequest request) {
        return service.updateAvailabilitySlot(principal.getName(), slotId, request);
    }

    @DeleteMapping("/availability/slots/{slotId}")
    public ResponseEntity<Void> removeSlot(Principal principal, @PathVariable Long slotId) {
        service.removeAvailabilitySlot(principal.getName(), slotId);
        return ResponseEntity.noContent().build();
    }
}
