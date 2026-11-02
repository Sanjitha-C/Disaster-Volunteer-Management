package edu.dvc.config;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.Skill;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.domain.VolunteerSkill;
import edu.dvc.repository.SkillRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import edu.dvc.repository.VolunteerSkillRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adds missing local demo identities once; existing accounts and passwords are never overwritten. */
@Component
@Profile("demo")
public class DemoAccountSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoAccountSeeder.class);
    private final UserAccountRepository accounts;
    private final VolunteerProfileRepository profiles;
    private final SkillRepository skills;
    private final VolunteerSkillRepository volunteerSkills;
    private final PasswordEncoder passwordEncoder;

    @Value("${dvc.demo.admin.email:admin@gmail.com}") private String adminEmail;
    @Value("${dvc.demo.admin.password:Admin@12345}") private String adminPassword;
    @Value("${dvc.demo.volunteer.email:volunteer@gmail.com}") private String volunteerEmail;
    @Value("${dvc.demo.volunteer.password:Volunteer@12345}") private String volunteerPassword;
    @Value("${dvc.demo.user.email:user@gmail.com}") private String userEmail;
    @Value("${dvc.demo.user.password:User@12345}") private String userPassword;

    public DemoAccountSeeder(UserAccountRepository accounts, VolunteerProfileRepository profiles,
                             SkillRepository skills, VolunteerSkillRepository volunteerSkills,
                             PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.profiles = profiles;
        this.skills = skills;
        this.volunteerSkills = volunteerSkills;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAccount(adminEmail, adminPassword, "DVC System Administrator", "9000000001",
            AccountRole.ADMIN, "Disaster Management");
        UserAccount volunteer = seedAccount(volunteerEmail, volunteerPassword, "Demo Volunteer", "9000000002",
            AccountRole.VOLUNTEER, null);
        seedAccount(userEmail, userPassword, "Demo User", "9000000003", AccountRole.USER, null);
        seedVolunteerProfile(volunteer);
    }

    private UserAccount seedAccount(String email, String rawPassword, String name, String phone,
                                    AccountRole role, String department) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        UserAccount account = accounts.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (account != null) {
            log.info("Demo account already exists for {}; leaving its account and password unchanged", normalizedEmail);
            return account;
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException("Demo password configuration must not be empty");
        }
        account = new UserAccount();
        account.setFullName(name);
        account.setEmail(normalizedEmail);
        account.setPhone(phone);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setDepartment(department);
        UserAccount saved = accounts.save(account);
        log.info("Seeded missing {} demo account ({})", role, normalizedEmail);
        return saved;
    }

    private void seedVolunteerProfile(UserAccount account) {
        if (account.getRole() != AccountRole.VOLUNTEER) return;
        VolunteerProfile profile = profiles.findByUserId(account.getId()).orElseGet(() -> {
            VolunteerProfile created = new VolunteerProfile();
            created.setUser(account);
            created.setAddress("Coimbatore, Tamil Nadu");
            created.setAvailability(AvailabilityStatus.AVAILABLE);
            return profiles.save(created);
        });
        seedSkill(profile, "First Aid");
        seedSkill(profile, "Rescue Support");
    }

    private void seedSkill(VolunteerProfile profile, String name) {
        Skill skill = skills.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill created = new Skill();
            created.setName(name);
            return skills.save(created);
        });
        if (!volunteerSkills.existsByVolunteerIdAndSkillId(profile.getId(), skill.getId())) {
            VolunteerSkill link = new VolunteerSkill();
            link.setVolunteer(profile);
            link.setSkill(skill);
            volunteerSkills.save(link);
        }
    }
}
