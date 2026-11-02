package edu.dvc.admin;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.EmergencyRequestStatus;
import edu.dvc.domain.ResourceStatus;
import edu.dvc.domain.TaskStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.EmergencyRequestRepository;
import edu.dvc.repository.ResourceItemRepository;
import edu.dvc.repository.TaskAssignmentRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerFeedbackRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminManagementService {
    private final UserAccountRepository accounts;
    private final VolunteerProfileRepository profiles;
    private final EmergencyRequestRepository requests;
    private final TaskAssignmentRepository assignments;
    private final ResourceItemRepository resources;
    private final VolunteerFeedbackRepository feedback;

    public AdminManagementService(UserAccountRepository accounts, VolunteerProfileRepository profiles,
            EmergencyRequestRepository requests, TaskAssignmentRepository assignments,
            ResourceItemRepository resources, VolunteerFeedbackRepository feedback) {
        this.accounts=accounts; this.profiles=profiles; this.requests=requests;
        this.assignments=assignments; this.resources=resources; this.feedback=feedback;
    }

    @Transactional(readOnly=true)
    public DashboardReportDto report() {
        List<DashboardReportDto.CountDto> requestCounts = java.util.Arrays.stream(EmergencyRequestStatus.values())
            .map(s -> new DashboardReportDto.CountDto(s.name(), requests.countByStatus(s))).toList();
        List<DashboardReportDto.CountDto> assignmentCounts = java.util.Arrays.stream(TaskStatus.values())
            .map(s -> new DashboardReportDto.CountDto(s.name(), assignments.countByStatus(s))).toList();
        List<edu.dvc.domain.VolunteerFeedback> reviews = feedback.findAll();
        double average = reviews.isEmpty() ? 0 : Math.round(reviews.stream().mapToInt(edu.dvc.domain.VolunteerFeedback::getRating).average().orElse(0) * 10.0) / 10.0;
        long activeVolunteers = accounts.countByRoleAndAccountStatus(AccountRole.VOLUNTEER, AccountStatus.ACTIVE);
        long availableVolunteers = profiles.findByAvailability(AvailabilityStatus.AVAILABLE).stream()
            .filter(p -> p.getUser().getRole() == AccountRole.VOLUNTEER && p.getUser().getAccountStatus() == AccountStatus.ACTIVE).count();
        return new DashboardReportDto(accounts.countByAccountStatus(AccountStatus.ACTIVE), activeVolunteers,
            availableVolunteers, requests.countByStatus(EmergencyRequestStatus.PENDING),
            assignments.countByStatus(TaskStatus.ASSIGNED)+assignments.countByStatus(TaskStatus.ACCEPTED)+assignments.countByStatus(TaskStatus.IN_PROGRESS),
            requests.countByStatus(EmergencyRequestStatus.COMPLETED), resources.countByStatus(ResourceStatus.AVAILABLE),
            average, reviews.size(), requestCounts, assignmentCounts);
    }

    @Transactional(readOnly=true)
    public List<AdminUserDto> users() {
        return accounts.findAll().stream().sorted(Comparator.comparing(UserAccount::getCreatedAt).reversed()).map(this::toDto).toList();
    }

    @Transactional
    public AdminUserDto updateAccess(Long id, String adminEmail, UpdateUserAccessRequest input) {
        UserAccount target = accounts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        String currentAdminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        boolean leavingActiveAdmin = target.getRole() == AccountRole.ADMIN && target.getAccountStatus() == AccountStatus.ACTIVE
            && (input.role() != AccountRole.ADMIN || input.accountStatus() != AccountStatus.ACTIVE);
        if (leavingActiveAdmin && accounts.countByRoleAndAccountStatus(AccountRole.ADMIN, AccountStatus.ACTIVE) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "At least one active Admin account must remain");
        }
        if (target.getEmail().equalsIgnoreCase(currentAdminEmail)
                && (input.role() != AccountRole.ADMIN || input.accountStatus() != AccountStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot remove or deactivate your own Admin account");
        }
        target.setRole(input.role());
        target.setAccountStatus(input.accountStatus());
        if (input.role() == AccountRole.VOLUNTEER && profiles.findByUserId(target.getId()).isEmpty()) {
            VolunteerProfile profile = new VolunteerProfile(); profile.setUser(target); profile.setAvailability(AvailabilityStatus.UNAVAILABLE); profiles.save(profile);
        }
        return toDto(target);
    }

    private AdminUserDto toDto(UserAccount user) {
        return new AdminUserDto(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getDepartment(), user.getRole(), user.getAccountStatus(), user.getCreatedAt());
    }
}
