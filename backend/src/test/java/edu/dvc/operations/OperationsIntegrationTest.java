package edu.dvc.operations;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.EmergencyRequest;
import edu.dvc.domain.EmergencyRequestStatus;
import edu.dvc.domain.EmergencyUrgency;
import edu.dvc.domain.TaskAssignment;
import edu.dvc.domain.TaskStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.EmergencyRequestRepository;
import edu.dvc.repository.TaskAssignmentRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class OperationsIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserAccountRepository accounts;
    @Autowired private VolunteerProfileRepository profiles;
    @Autowired private EmergencyRequestRepository requests;
    @Autowired private TaskAssignmentRepository assignments;
    @Autowired private PasswordEncoder encoder;

    @Test
    void volunteerCanProgressOwnedTaskAndRequesterCanReadNotificationsAndLeaveFeedback() throws Exception {
        UserAccount requester = account(AccountRole.USER); VolunteerProfile volunteer = volunteer(); TaskAssignment assignment = assignment(requester, volunteer);
        String base = "/api/volunteer/me/assignments/" + assignment.getId() + "/status";
        mvc.perform(put(base).with(user(volunteer.getUser().getEmail()).roles("VOLUNTEER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACCEPTED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACCEPTED"));
        mvc.perform(put(base).with(user(volunteer.getUser().getEmail()).roles("VOLUNTEER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk());
        mvc.perform(put(base).with(user(volunteer.getUser().getEmail()).roles("VOLUNTEER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/notifications/me").with(user(requester.getEmail()).roles("USER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(post("/api/volunteer/me/assignments/" + assignment.getId() + "/feedback").with(user(volunteer.getUser().getEmail()).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5,\"comments\":\"Well coordinated\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.rating").value(5));
        mvc.perform(get("/api/admin/feedback").with(user("admin@gmail.com").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].requestTitle").value("Test emergency"));
    }

    @Test
    void taskTransitionsAndFeedbackOwnershipAreEnforced() throws Exception {
        UserAccount requester = account(AccountRole.USER); VolunteerProfile owner = volunteer(); VolunteerProfile other = volunteer();
        TaskAssignment assignment = assignment(requester, owner);
        String statusPath = "/api/volunteer/me/assignments/" + assignment.getId() + "/status";
        mvc.perform(put(statusPath).with(user(other.getUser().getEmail()).roles("VOLUNTEER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACCEPTED\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(put(statusPath).with(user(owner.getUser().getEmail()).roles("VOLUNTEER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/volunteer/me/assignments/" + assignment.getId() + "/feedback").with(user(other.getUser().getEmail()).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void adminCanManageInventoryAndOtherRolesAreForbidden() throws Exception {
        String path = "/api/admin/resources";
        String payload = "{\"name\":\"Water\",\"quantity\":25,\"unit\":\"cases\",\"status\":\"AVAILABLE\"}";
        mvc.perform(post(path).with(user("admin@gmail.com").roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(25));
        mvc.perform(get(path).with(user("user@gmail.com").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get(path).with(user("admin@gmail.com").roles("ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Water"));
    }

    @Test
    void adminCanCancelActiveAssignmentButCannotChangeItToAnArbitraryStatus() throws Exception {
        TaskAssignment assignment = assignment(account(AccountRole.USER), volunteer());
        String path = "/api/admin/assignments/" + assignment.getId() + "/status";
        mvc.perform(put(path).with(user("admin@gmail.com").roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(put(path).with(user("admin@gmail.com").roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isBadRequest());
    }

    private UserAccount account(AccountRole role) {
        String suffix = UUID.randomUUID().toString(); UserAccount item = new UserAccount();
        item.setFullName("Ops " + role + " " + suffix.substring(0, 8)); item.setEmail(role.name().toLowerCase()+"-"+suffix+"@example.test");
        item.setPhone("9000000011"); item.setPasswordHash(encoder.encode("TestPassword@123")); item.setRole(role); item.setAccountStatus(AccountStatus.ACTIVE);
        return accounts.save(item);
    }
    private VolunteerProfile volunteer() { UserAccount user = account(AccountRole.VOLUNTEER); VolunteerProfile p = new VolunteerProfile(); p.setUser(user); p.setAvailability(AvailabilityStatus.AVAILABLE); return profiles.save(p); }
    private TaskAssignment assignment(UserAccount requester, VolunteerProfile volunteer) {
        EmergencyRequest request = new EmergencyRequest(); request.setRequester(requester); request.setTitle("Test emergency"); request.setDescription("Test description"); request.setLocation("Test location"); request.setUrgency(EmergencyUrgency.MEDIUM); request.setStatus(EmergencyRequestStatus.ASSIGNED); request = requests.save(request);
        TaskAssignment assignment = new TaskAssignment(); assignment.setRequest(request); assignment.setVolunteer(volunteer); assignment.setStatus(TaskStatus.ASSIGNED); return assignments.save(assignment);
    }
}
