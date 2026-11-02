package edu.dvc.operations;

import edu.dvc.coordination.AssignmentDto;
import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.EmergencyRequestStatus;
import edu.dvc.domain.Notification;
import edu.dvc.domain.ResourceItem;
import edu.dvc.domain.TaskAssignment;
import edu.dvc.domain.TaskStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerFeedback;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.NotificationRepository;
import edu.dvc.repository.ResourceItemRepository;
import edu.dvc.repository.TaskAssignmentRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerFeedbackRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperationsService {
    private final UserAccountRepository accounts;
    private final VolunteerProfileRepository volunteers;
    private final TaskAssignmentRepository assignments;
    private final NotificationRepository notifications;
    private final ResourceItemRepository resources;
    private final VolunteerFeedbackRepository feedback;

    public OperationsService(UserAccountRepository accounts, VolunteerProfileRepository volunteers,
            TaskAssignmentRepository assignments, NotificationRepository notifications,
            ResourceItemRepository resources, VolunteerFeedbackRepository feedback) {
        this.accounts = accounts; this.volunteers = volunteers; this.assignments = assignments;
        this.notifications = notifications; this.resources = resources; this.feedback = feedback;
    }

    @Transactional
    public TaskAssignment updateTaskStatus(String email, Long assignmentId, TaskStatus next) {
        VolunteerProfile volunteer = requireVolunteer(email);
        TaskAssignment assignment = assignments.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!assignment.getVolunteer().getId().equals(volunteer.getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        TaskStatus current = assignment.getStatus();
        boolean allowed = (current == TaskStatus.ASSIGNED && next == TaskStatus.ACCEPTED)
            || (current == TaskStatus.ACCEPTED && (next == TaskStatus.IN_PROGRESS || next == TaskStatus.CANCELLED))
            || (current == TaskStatus.IN_PROGRESS && (next == TaskStatus.COMPLETED || next == TaskStatus.CANCELLED));
        if (!allowed) throw new ResponseStatusException(HttpStatus.CONFLICT, "This task status transition is not allowed");
        assignment.setStatus(next);
        if (next == TaskStatus.IN_PROGRESS) assignment.getRequest().setStatus(EmergencyRequestStatus.IN_PROGRESS);
        if (next == TaskStatus.COMPLETED) assignment.getRequest().setStatus(EmergencyRequestStatus.COMPLETED);
        if (next == TaskStatus.CANCELLED) assignment.getRequest().setStatus(EmergencyRequestStatus.PENDING);
        notify(assignment.getRequest().getRequester(), "Task update: " + assignment.getRequest().getTitle(),
            volunteer.getUser().getFullName() + " marked the assigned task " + next.name().toLowerCase(Locale.ROOT).replace('_', ' ') + ".");
        return assignment;
    }

    @Transactional
    public AssignmentDto cancelTaskAsAdmin(Long assignmentId) {
        TaskAssignment assignment = assignments.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (assignment.getStatus() == TaskStatus.COMPLETED || assignment.getStatus() == TaskStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed or cancelled tasks cannot be cancelled again");
        }
        assignment.setStatus(TaskStatus.CANCELLED);
        assignment.getRequest().setStatus(EmergencyRequestStatus.PENDING);
        notify(assignment.getRequest().getRequester(), "Task cancelled: " + assignment.getRequest().getTitle(), "The coordination team cancelled this volunteer assignment.");
        notify(assignment.getVolunteer().getUser(), "Assignment cancelled", "Your assignment for " + assignment.getRequest().getTitle() + " was cancelled by the coordination team.");
        VolunteerProfile volunteer = assignment.getVolunteer();
        return new AssignmentDto(assignment.getId(), volunteer.getId(), volunteer.getUser().getFullName(),
            volunteer.getUser().getEmail(), assignment.getStatus(), assignment.getAssignedAt());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> myNotifications(String email) {
        UserAccount account = requireAccount(email, null);
        return notifications.findByRecipientIdOrderByCreatedAtDesc(account.getId()).stream()
            .map(n -> new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt())).toList();
    }

    @Transactional
    public NotificationDto markRead(String email, Long id) {
        UserAccount account = requireAccount(email, null);
        Notification item = notifications.findById(id).filter(n -> n.getRecipient().getId().equals(account.getId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        item.setRead(true);
        return new NotificationDto(item.getId(), item.getTitle(), item.getMessage(), item.isRead(), item.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> allResources() { return resources.findAllByOrderByNameAsc().stream().map(this::resourceDto).toList(); }

    @Transactional
    public ResourceDto createResource(ResourceRequest request) {
        ResourceItem item = new ResourceItem(); apply(item, request); return resourceDto(resources.save(item));
    }

    @Transactional
    public ResourceDto updateResource(Long id, ResourceRequest request) {
        ResourceItem item = resources.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        apply(item, request); return resourceDto(item);
    }

    @Transactional
    public void deleteResource(Long id) {
        if (!resources.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        resources.deleteById(id);
    }

    @Transactional
    public FeedbackDto submitFeedback(String email, Long assignmentId, FeedbackRequest request) {
        VolunteerProfile volunteer = requireVolunteer(email);
        TaskAssignment assignment = assignments.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!assignment.getVolunteer().getId().equals(volunteer.getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        if (assignment.getStatus() != TaskStatus.COMPLETED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback is available after task completion");
        if (feedback.existsByAssignmentId(assignmentId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback was already submitted for this task");
        VolunteerFeedback item = new VolunteerFeedback(); item.setAssignment(assignment); item.setRating(request.rating());
        item.setComments(request.comments() == null || request.comments().isBlank() ? null : request.comments().trim());
        return feedbackDto(feedback.save(item));
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> allFeedback() { return feedback.findAllByOrderByCreatedAtDesc().stream().map(this::feedbackDto).toList(); }

    @Transactional(readOnly = true)
    public List<FeedbackDto> myFeedback(String email) {
        VolunteerProfile volunteer = requireVolunteer(email);
        return feedback.findByAssignmentVolunteerIdOrderByCreatedAtDesc(volunteer.getId()).stream()
            .map(this::feedbackDto).toList();
    }

    private UserAccount requireAccount(String email, AccountRole role) {
        UserAccount account = accounts.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        if (role != null && account.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account cannot access this operation");
        if (account.getAccountStatus() != AccountStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        return account;
    }
    private VolunteerProfile requireVolunteer(String email) {
        UserAccount account = requireAccount(email, AccountRole.VOLUNTEER);
        return volunteers.findByUserId(account.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer profile not found"));
    }
    private void apply(ResourceItem item, ResourceRequest request) {
        item.setName(request.name().trim()); item.setQuantity(request.quantity()); item.setUnit(request.unit().trim()); item.setStatus(request.status());
    }
    private ResourceDto resourceDto(ResourceItem item) { return new ResourceDto(item.getId(), item.getName(), item.getQuantity(), item.getUnit(), item.getStatus()); }
    private FeedbackDto feedbackDto(VolunteerFeedback item) {
        TaskAssignment assignment = item.getAssignment(); VolunteerProfile volunteer = assignment.getVolunteer();
        return new FeedbackDto(item.getId(), assignment.getId(), volunteer.getId(), volunteer.getUser().getFullName(), assignment.getRequest().getTitle(), item.getRating(), item.getComments(), item.getCreatedAt());
    }
    private void notify(UserAccount recipient, String title, String message) {
        Notification item = new Notification(); item.setRecipient(recipient); item.setTitle(title); item.setMessage(message); notifications.save(item);
    }
}
