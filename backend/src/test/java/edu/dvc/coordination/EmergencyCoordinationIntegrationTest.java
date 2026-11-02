package edu.dvc.coordination;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.EmergencyUrgency;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.List;
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
class EmergencyCoordinationIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserAccountRepository accounts;
    @Autowired private VolunteerProfileRepository profiles;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void communityUserCanSubmitAndTrackOwnRequestOnly() throws Exception {
        String email = createAccount(AccountRole.USER).getEmail();
        String payload = """
            {"title":"Flooded street","description":"Residents need assistance","location":"River Road",
             "urgency":"HIGH","requiredSkills":["First Aid","Driving"]}
            """;
        mvc.perform(post("/api/user/requests").with(user(email).roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Flooded street"))
            .andExpect(jsonPath("$.status").value("PENDING")).andExpect(jsonPath("$.requiredSkills.length()").value(2));
        mvc.perform(get("/api/user/requests").with(user(email).roles("USER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].requesterEmail").value(email));
        mvc.perform(get("/api/user/requests").with(user("volunteer@gmail.com").roles("VOLUNTEER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReviewAndAssignOnlyAvailableVolunteerAndVolunteerSeesAssignment() throws Exception {
        UserAccount requester = createAccount(AccountRole.USER);
        String payload = """
            {"title":"Medical supplies","description":"Need supplies delivered","location":"Central Park",
             "urgency":"CRITICAL","requiredSkills":["First Aid"]}
            """;
        String response = mvc.perform(post("/api/user/requests").with(user(requester.getEmail()).roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        EmergencyRequestDto created = mapper.readValue(response, EmergencyRequestDto.class);
        VolunteerProfile volunteer = createVolunteer(AvailabilityStatus.AVAILABLE);
        mvc.perform(get("/api/admin/requests").with(user("admin@gmail.com").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + created.id() + ")]" ).exists());
        mvc.perform(post("/api/admin/requests/" + created.id() + "/assignments").with(user("admin@gmail.com").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"volunteerId\":" + volunteer.getId() + "}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.volunteerId").value(volunteer.getId()));
        mvc.perform(get("/api/volunteer/me/assignments").with(user(volunteer.getUser().getEmail()).roles("VOLUNTEER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("Medical supplies"));
        mvc.perform(post("/api/admin/requests/" + created.id() + "/assignments").with(user("admin@gmail.com").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"volunteerId\":" + volunteer.getId() + "}"))
            .andExpect(status().isConflict());
    }

    @Test
    void inactiveVolunteersCannotBeAssignedAndAnonymousCannotSubmit() throws Exception {
        mvc.perform(post("/api/user/requests").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Help\",\"description\":\"Details\",\"location\":\"Town\",\"urgency\":\"LOW\"}"))
            .andExpect(status().isUnauthorized());
        UserAccount requester = createAccount(AccountRole.USER);
        String response = mvc.perform(post("/api/user/requests").with(user(requester.getEmail()).roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Help\",\"description\":\"Details\",\"location\":\"Town\",\"urgency\":\"LOW\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        EmergencyRequestDto created = mapper.readValue(response, EmergencyRequestDto.class);
        VolunteerProfile volunteer = createVolunteer(AvailabilityStatus.UNAVAILABLE);
        mvc.perform(post("/api/admin/requests/" + created.id() + "/assignments").with(user("admin@gmail.com").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"volunteerId\":" + volunteer.getId() + "}"))
            .andExpect(status().isConflict());
    }

    private UserAccount createAccount(AccountRole role) {
        String suffix = UUID.randomUUID().toString();
        UserAccount account = new UserAccount();
        account.setFullName("Test " + role + " " + suffix.substring(0, 8));
        account.setEmail(role.name().toLowerCase() + "-" + suffix + "@example.test");
        account.setPhone("9000000088");
        account.setPasswordHash(passwordEncoder.encode("TestPassword@123"));
        account.setRole(role);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return accounts.save(account);
    }

    private VolunteerProfile createVolunteer(AvailabilityStatus availability) {
        UserAccount account = createAccount(AccountRole.VOLUNTEER);
        VolunteerProfile volunteer = new VolunteerProfile();
        volunteer.setUser(account);
        volunteer.setAvailability(availability);
        return profiles.save(volunteer);
    }
}
