package edu.dvc.coordination;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.EmergencyRequest;
import edu.dvc.domain.EmergencyRequestSkill;
import edu.dvc.domain.EmergencyRequestStatus;
import edu.dvc.domain.EmergencyUrgency;
import edu.dvc.domain.Notification;
import edu.dvc.domain.Skill;
import edu.dvc.domain.TaskAssignment;
import edu.dvc.domain.TaskStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.EmergencyRequestRepository;
import edu.dvc.repository.EmergencyRequestSkillRepository;
import edu.dvc.repository.NotificationRepository;
import edu.dvc.repository.SkillRepository;
import edu.dvc.repository.TaskAssignmentRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmergencyCoordinationService {
    private final EmergencyRequestRepository requests;
    private final EmergencyRequestSkillRepository requestSkills;
    private final TaskAssignmentRepository assignments;
    private final UserAccountRepository accounts;
    private final VolunteerProfileRepository volunteers;
    private final SkillRepository skills;
    private final NotificationRepository notifications;

    public EmergencyCoordinationService(EmergencyRequestRepository requests,
            EmergencyRequestSkillRepository requestSkills, TaskAssignmentRepository assignments,
            UserAccountRepository accounts, VolunteerProfileRepository volunteers, SkillRepository skills,
            NotificationRepository notifications) {
        this.requests = requests;
        this.requestSkills = requestSkills;
        this.assignments = assignments;
        this.accounts = accounts;
        this.volunteers = volunteers;
        this.skills = skills;
        this.notifications = notifications;
    }

    @Transactional
    public EmergencyRequestDto createRequest(String email, EmergencyRequestCreateRequest input) {
        UserAccount requester = requireAccount(email, AccountRole.USER);
        EmergencyRequest request = new EmergencyRequest();
        request.setRequester(requester);
        request.setTitle(input.title().trim());
        request.setDescription(input.description().trim());
        request.setLocation(input.location().trim());
        request.setUrgency(input.urgency());
        request = requests.save(request);
        if (input.requiredSkills() != null) {
            for (String rawName : input.requiredSkills()) {
                String name = rawName.trim();
                if (name.isEmpty()) continue;
                Skill skill = skills.findByNameIgnoreCase(name).orElseGet(() -> {
                    Skill created = new Skill();
                    created.setName(name);
                    return skills.save(created);
                });
                boolean alreadyLinked = requestSkills.findByRequestId(request.getId()).stream()
                    .anyMatch(link -> link.getSkill().getId().equals(skill.getId()));
                if (!alreadyLinked) {
                    EmergencyRequestSkill link = new EmergencyRequestSkill();
                    link.setRequest(request);
                    link.setSkill(skill);
                    requestSkills.save(link);
                }
            }
        }
        return toDto(request);
    }

    @Transactional(readOnly = true)
    public List<EmergencyRequestDto> myRequests(String email) {
        UserAccount requester = requireAccount(email, AccountRole.USER);
        return requests.findByRequesterIdOrderByCreatedAtDesc(requester.getId()).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EmergencyRequestDto> allRequests() {
        return requests.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public AssignmentDto assign(Long requestId, Long volunteerId) {
        EmergencyRequest request = requests.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emergency request not found"));
        VolunteerProfile volunteer = volunteers.findById(volunteerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));
        if (volunteer.getUser().getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active volunteers can be assigned");
        }
        if (volunteer.getAvailability() != edu.dvc.domain.AvailabilityStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Volunteer must be marked available before assignment");
        }
        if (request.getStatus() == EmergencyRequestStatus.COMPLETED || request.getStatus() == EmergencyRequestStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed requests cannot receive assignments");
        }
        if (assignments.existsByRequestIdAndVolunteerId(requestId, volunteerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This volunteer is already assigned to the request");
        }
        TaskAssignment assignment = new TaskAssignment();
        assignment.setRequest(request);
        assignment.setVolunteer(volunteer);
        assignment.setStatus(TaskStatus.ASSIGNED);
        assignment = assignments.save(assignment);
        request.setStatus(EmergencyRequestStatus.ASSIGNED);
        addNotification(volunteer.getUser(), "New response assignment", "You have been assigned to: " + request.getTitle());
        addNotification(request.getRequester(), "Volunteer assigned", volunteer.getUser().getFullName() + " has been assigned to your request.");
        return toAssignmentDto(assignment);
    }

    @Transactional(readOnly = true)
    public List<EmergencyRequestDto> volunteerAssignments(String email) {
        UserAccount account = requireAccount(email, AccountRole.VOLUNTEER);
        VolunteerProfile profile = volunteers.findByUserId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer profile not found"));
        return assignments.findByVolunteerIdOrderByAssignedAtDesc(profile.getId()).stream()
            .map(TaskAssignment::getRequest).distinct().map(this::toDto).toList();
    }

    private UserAccount requireAccount(String email, AccountRole role) {
        UserAccount account = accounts.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        if (account.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account cannot access this operation");
        if (account.getAccountStatus() != AccountStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        return account;
    }

    private EmergencyRequestDto toDto(EmergencyRequest request) {
        List<RequestSkillDto> requiredSkills = requestSkills.findByRequestId(request.getId()).stream()
            .map(link -> new RequestSkillDto(link.getSkill().getId(), link.getSkill().getName())).toList();
        List<AssignmentDto> requestAssignments = assignments.findByRequestIdOrderByAssignedAtDesc(request.getId()).stream()
            .map(this::toAssignmentDto).toList();
        return new EmergencyRequestDto(request.getId(), request.getTitle(), request.getDescription(),
            request.getLocation(), request.getUrgency(), request.getStatus(), request.getCreatedAt(),
            request.getRequester().getFullName(), request.getRequester().getEmail(), requiredSkills, requestAssignments);
    }

    private AssignmentDto toAssignmentDto(TaskAssignment assignment) {
        VolunteerProfile volunteer = assignment.getVolunteer();
        return new AssignmentDto(assignment.getId(), volunteer.getId(), volunteer.getUser().getFullName(),
            volunteer.getUser().getEmail(), assignment.getStatus(), assignment.getAssignedAt());
    }

    private void addNotification(UserAccount recipient, String title, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notifications.save(notification);
    }
}
