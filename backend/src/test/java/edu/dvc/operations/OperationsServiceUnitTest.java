package edu.dvc.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.EmergencyRequest;
import edu.dvc.domain.TaskAssignment;
import edu.dvc.domain.TaskStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.NotificationRepository;
import edu.dvc.repository.ResourceItemRepository;
import edu.dvc.repository.TaskAssignmentRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerFeedbackRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OperationsServiceUnitTest {
    @Mock private UserAccountRepository accounts;
    @Mock private VolunteerProfileRepository profiles;
    @Mock private TaskAssignmentRepository assignments;
    @Mock private NotificationRepository notifications;
    @Mock private ResourceItemRepository resources;
    @Mock private VolunteerFeedbackRepository feedback;
    private OperationsService service;
    private UserAccount volunteerAccount;
    private VolunteerProfile volunteer;
    private TaskAssignment assignment;

    @BeforeEach
    void setup() {
        service = new OperationsService(accounts, profiles, assignments, notifications, resources, feedback);
        volunteerAccount = new UserAccount(); volunteerAccount.setId(22L); volunteerAccount.setEmail("volunteer@example.test");
        volunteerAccount.setRole(AccountRole.VOLUNTEER); volunteerAccount.setAccountStatus(AccountStatus.ACTIVE);
        volunteer = new VolunteerProfile(); volunteer.setId(33L); volunteer.setUser(volunteerAccount);
        UserAccount requester = new UserAccount(); requester.setId(44L); requester.setEmail("user@example.test");
        assignment = new TaskAssignment(); assignment.setVolunteer(volunteer);
        EmergencyRequest request = new EmergencyRequest(); request.setTitle("Emergency"); request.setRequester(requester);
        assignment.setRequest(request); assignment.setStatus(TaskStatus.ASSIGNED);
    }

    @Test
    void rejectsIllegalStatusTransitionWithoutPersistingIt() {
        when(accounts.findByEmailIgnoreCase("volunteer@example.test")).thenReturn(Optional.of(volunteerAccount));
        when(profiles.findByUserId(22L)).thenReturn(Optional.of(volunteer));
        when(assignments.findById(55L)).thenReturn(Optional.of(assignment));
        assertThrows(ResponseStatusException.class, () -> service.updateTaskStatus("volunteer@example.test", 55L, TaskStatus.COMPLETED));
        verify(assignments, never()).save(any());
        verify(notifications, never()).save(any());
        assertEquals(TaskStatus.ASSIGNED, assignment.getStatus());
    }

    @Test
    void rejectsAnotherVolunteersAssignmentAsNotFound() {
        when(accounts.findByEmailIgnoreCase("volunteer@example.test")).thenReturn(Optional.of(volunteerAccount));
        when(profiles.findByUserId(22L)).thenReturn(Optional.of(volunteer));
        VolunteerProfile otherVolunteer = new VolunteerProfile(); otherVolunteer.setId(77L);
        assignment.setVolunteer(otherVolunteer);
        when(assignments.findById(55L)).thenReturn(Optional.of(assignment));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.updateTaskStatus("volunteer@example.test", 55L, TaskStatus.ACCEPTED));
        assertEquals(404, error.getStatusCode().value());
        verify(assignments, never()).save(any());
    }
}
