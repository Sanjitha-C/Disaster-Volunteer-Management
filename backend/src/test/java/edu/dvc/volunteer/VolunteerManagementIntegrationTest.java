package edu.dvc.volunteer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class VolunteerManagementIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserAccountRepository accounts;
    @Autowired private VolunteerProfileRepository profiles;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void volunteerCanReadAndUpdateOwnProfile() throws Exception {
        VolunteerProfile profile = createVolunteer();
        String email = profile.getUser().getEmail();
        mvc.perform(get("/api/volunteer/me/profile").with(user(email).roles("VOLUNTEER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.address").isEmpty());

        UpdateVolunteerProfileRequest request = new UpdateVolunteerProfileRequest(
            "Updated Volunteer", "+91 9000000077", "Coimbatore, Tamil Nadu");
        mvc.perform(put("/api/volunteer/me/profile").with(user(email).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Volunteer"))
            .andExpect(jsonPath("$.profileCompletionPercent").value(100));
    }

    @Test
    void volunteerCanAddRenameAndRemoveSkillsAndCannotEditAnotherVolunteersSkill() throws Exception {
        VolunteerProfile owner = createVolunteer();
        String ownerEmail = owner.getUser().getEmail();
        SkillRequest first = new SkillRequest("First Aid " + UUID.randomUUID());
        MvcResult created = mvc.perform(post("/api/volunteer/me/skills").with(user(ownerEmail).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(first.name().trim()))
            .andReturn();
        VolunteerSkillDto link = objectMapper.readValue(created.getResponse().getContentAsString(), VolunteerSkillDto.class);

        mvc.perform(post("/api/volunteer/me/skills").with(user(ownerEmail).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isConflict());

        SkillRequest renamed = new SkillRequest("Advanced Rescue " + UUID.randomUUID());
        mvc.perform(put("/api/volunteer/me/skills/" + link.id()).with(user(ownerEmail).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(renamed)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(renamed.name()));

        String otherEmail = createVolunteer().getUser().getEmail();
        mvc.perform(delete("/api/volunteer/me/skills/" + link.id()).with(user(otherEmail).roles("VOLUNTEER")).with(csrf()))
            .andExpect(status().isNotFound());

        mvc.perform(delete("/api/volunteer/me/skills/" + link.id()).with(user(ownerEmail).roles("VOLUNTEER")).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/volunteer/me/skills").with(user(ownerEmail).roles("VOLUNTEER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + link.id() + ")]").doesNotExist());
    }

    @Test
    void availabilityStatusAndTimeSlotsCanBeManagedWithoutOverlaps() throws Exception {
        String email = createVolunteer().getUser().getEmail();
        mvc.perform(get("/api/volunteer/me/availability").with(user(email).roles("VOLUNTEER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNAVAILABLE"));
        mvc.perform(put("/api/volunteer/me/availability").with(user(email).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"AVAILABLE\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AVAILABLE"));

        LocalDateTime start = LocalDateTime.now().plusDays(5).withNano(0);
        AvailabilitySlotRequest first = new AvailabilitySlotRequest(start, start.plusHours(2));
        MvcResult result = mvc.perform(post("/api/volunteer/me/availability/slots").with(user(email).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isCreated()).andReturn();
        AvailabilitySlotDto slot = objectMapper.readValue(result.getResponse().getContentAsString(), AvailabilitySlotDto.class);

        AvailabilitySlotRequest overlap = new AvailabilitySlotRequest(start.plusHours(1), start.plusHours(3));
        mvc.perform(post("/api/volunteer/me/availability/slots").with(user(email).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(overlap)))
            .andExpect(status().isConflict());

        AvailabilitySlotRequest updated = new AvailabilitySlotRequest(start.plusDays(1), start.plusDays(1).plusHours(1));
        mvc.perform(put("/api/volunteer/me/availability/slots/" + slot.id()).with(user(email).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk());
        mvc.perform(delete("/api/volunteer/me/availability/slots/" + slot.id()).with(user(email).roles("VOLUNTEER")).with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    void invalidTimeRangesAndNonVolunteerRolesAreRejected() throws Exception {
        String email = createVolunteer().getUser().getEmail();
        LocalDateTime start = LocalDateTime.now().plusDays(3).withNano(0);
        AvailabilitySlotRequest invalid = new AvailabilitySlotRequest(start.plusHours(2), start.plusHours(1));
        mvc.perform(post("/api/volunteer/me/availability/slots").with(user(email).roles("VOLUNTEER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/volunteer/me/profile").with(user("community@example.test").roles("USER")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/volunteer/me/profile"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanViewVolunteerAvailabilityButVolunteersCannot() throws Exception {
        VolunteerProfile profile = createVolunteer();
        mvc.perform(get("/api/admin/volunteers/availability").with(user("admin@gmail.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.email == '" + profile.getUser().getEmail() + "')]").exists());
        mvc.perform(get("/api/admin/volunteers/availability").with(user(profile.getUser().getEmail()).roles("VOLUNTEER")))
            .andExpect(status().isForbidden());
    }

    private VolunteerProfile createVolunteer() {
        String suffix = UUID.randomUUID().toString();
        UserAccount account = new UserAccount();
        account.setFullName("Test Volunteer " + suffix.substring(0, 8));
        account.setEmail("volunteer-" + suffix + "@example.test");
        account.setPhone("9000000077");
        account.setPasswordHash(passwordEncoder.encode("TestPassword@123"));
        account.setRole(AccountRole.VOLUNTEER);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = accounts.save(account);
        VolunteerProfile profile = new VolunteerProfile();
        profile.setUser(account);
        profile.setAvailability(AvailabilityStatus.UNAVAILABLE);
        return profiles.save(profile);
    }
}
