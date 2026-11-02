package edu.dvc.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.config.DemoAccountSeeder;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class AuthSecurityIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserAccountRepository accounts;
    @Autowired private VolunteerProfileRepository profiles;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DemoAccountSeeder demoAccountSeeder;

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void volunteerCannotCallAdminEndpoints() throws Exception {
        mvc.perform(get("/api/admin/users").with(user("volunteer@gmail.com").roles("VOLUNTEER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void stateChangingRequestsRequireCsrfToken() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void registrationCreatesVolunteerWithHashedPasswordAndIgnoresSubmittedRole() throws Exception {
        String email = "new-volunteer-" + UUID.randomUUID() + "@example.test";
        RegisterVolunteerRequest payload = new RegisterVolunteerRequest(
            "Test Volunteer", email, "9000000099", "SafePass@123", "Coimbatore");
        String body = objectMapper.writeValueAsString(payload).replace("}", ",\"role\":\"ADMIN\"}");

        MvcResult result = mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.role").value("VOLUNTEER"))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
            .andReturn();

        UserAccount saved = accounts.findByEmailIgnoreCase(email).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("SafePass@123", saved.getPasswordHash()));
        org.junit.jupiter.api.Assertions.assertFalse(saved.getPasswordHash().equals("SafePass@123"));
        org.junit.jupiter.api.Assertions.assertTrue(profiles.findByUserId(saved.getId()).isPresent());
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        String email = "duplicate-" + UUID.randomUUID() + "@example.test";
        RegisterVolunteerRequest payload = new RegisterVolunteerRequest(
            "First Volunteer", email, "9000000098", "SafePass@123", "Coimbatore");
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isConflict());
    }

    @Test
    void malformedRegistrationIsRejected() throws Exception {
        String body = "{\"fullName\":\"\",\"email\":\"bad-email\",\"phone\":\"x\",\"password\":\"short\"}";
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "admin@gmail.com,Admin@12345,ADMIN",
        "volunteer@gmail.com,Volunteer@12345,VOLUNTEER",
        "user@gmail.com,User@12345,USER"
    })
    void demoAccountsCanLogInAndSessionPersists(String email, String password, String role) throws Exception {
        String body = objectMapper.writeValueAsString(new AuthController.LoginRequest(email, password));
        MvcResult result = mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value(role))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
            .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void incorrectPasswordReturnsUnauthorized() throws Exception {
        String body = objectMapper.writeValueAsString(new AuthController.LoginRequest("user@gmail.com", "incorrect"));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        String body = objectMapper.writeValueAsString(new AuthController.LoginRequest("user@gmail.com", "User@12345"));
        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"));
        mvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void demoSeederIsIdempotentAndDoesNotResetPasswords() throws Exception {
        UserAccount admin = accounts.findByEmailIgnoreCase("admin@gmail.com").orElseThrow();
        String originalHash = admin.getPasswordHash();
        long originalAccountCount = accounts.count();
        demoAccountSeeder.run();
        UserAccount afterRestart = accounts.findByEmailIgnoreCase("admin@gmail.com").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(originalHash, afterRestart.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertEquals(originalAccountCount, accounts.count());
    }
}
