package edu.dvc.operations;

import edu.dvc.coordination.AssignmentDto;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OperationsController {
    private final OperationsService service;
    public OperationsController(OperationsService service) { this.service = service; }

    @PutMapping("/volunteer/me/assignments/{id}/status")
    public AssignmentDto updateStatus(Principal principal, @PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        var assignment = service.updateTaskStatus(principal.getName(), id, request.status());
        return new AssignmentDto(assignment.getId(), assignment.getVolunteer().getId(), assignment.getVolunteer().getUser().getFullName(), assignment.getVolunteer().getUser().getEmail(), assignment.getStatus(), assignment.getAssignedAt());
    }

    @GetMapping("/notifications/me")
    public List<NotificationDto> notifications(Principal principal) { return service.myNotifications(principal.getName()); }

    @PutMapping("/notifications/me/{id}/read")
    public NotificationDto markRead(Principal principal, @PathVariable Long id) { return service.markRead(principal.getName(), id); }

    @PostMapping("/volunteer/me/assignments/{id}/feedback")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public FeedbackDto feedback(Principal principal, @PathVariable Long id, @Valid @RequestBody FeedbackRequest request) {
        return service.submitFeedback(principal.getName(), id, request);
    }

    @GetMapping("/volunteer/me/feedback")
    public List<FeedbackDto> myFeedback(Principal principal) { return service.myFeedback(principal.getName()); }
}
