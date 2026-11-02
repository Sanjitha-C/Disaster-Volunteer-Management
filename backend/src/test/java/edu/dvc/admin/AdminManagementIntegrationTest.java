package edu.dvc.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.repository.UserAccountRepository;
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
class AdminManagementIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserAccountRepository accounts;
    @Autowired private PasswordEncoder encoder;

    @Test
    void adminReportReturnsCurrentSummaryAndOtherRolesAreForbidden() throws Exception {
        mvc.perform(get("/api/admin/reports/summary").with(user("admin@gmail.com").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.activeUsers").isNumber())
            .andExpect(jsonPath("$.requestStatuses").isArray()).andExpect(jsonPath("$.assignmentStatuses").isArray());
        mvc.perform(get("/api/admin/reports/summary").with(user("user@gmail.com").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanChangeAccountRoleAndStatusAndVolunteerPromotionCreatesProfile() throws Exception {
        UserAccount user = createUser();
        mvc.perform(get("/api/admin/users").with(user("admin@gmail.com").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.email == '" + user.getEmail() + "')]" ).exists());
        mvc.perform(put("/api/admin/users/" + user.getId() + "/access").with(user("admin@gmail.com").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"VOLUNTEER\",\"accountStatus\":\"ACTIVE\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("VOLUNTEER"));
        mvc.perform(get("/api/admin/volunteers/availability").with(user("admin@gmail.com").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.email == '" + user.getEmail() + "')]" ).exists());
        mvc.perform(put("/api/admin/users/" + user.getId() + "/access").with(user("user@gmail.com").roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"USER\",\"accountStatus\":\"SUSPENDED\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void activeAdminCannotRemoveOwnAccessOrDemoteTheLastAdmin() throws Exception {
        UserAccount admin = accounts.findByEmailIgnoreCase("admin@gmail.com").orElseThrow();
        mvc.perform(put("/api/admin/users/" + admin.getId() + "/access").with(user("admin@gmail.com").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"USER\",\"accountStatus\":\"ACTIVE\"}"))
            .andExpect(status().isConflict());
    }

    private UserAccount createUser() {
        String suffix=UUID.randomUUID().toString(); UserAccount user=new UserAccount(); user.setFullName("Managed User");
        user.setEmail("managed-"+suffix+"@example.test"); user.setPhone("9000000022"); user.setPasswordHash(encoder.encode("TestPassword@123"));
        user.setRole(AccountRole.USER); user.setAccountStatus(AccountStatus.ACTIVE); return accounts.save(user);
    }
}
