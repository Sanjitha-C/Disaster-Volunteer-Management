package edu.dvc.auth;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserAccountRepository accounts;
    private final VolunteerProfileRepository volunteerProfiles;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository accounts, VolunteerProfileRepository volunteerProfiles,
                       PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.volunteerProfiles = volunteerProfiles;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUser registerVolunteer(RegisterVolunteerRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (accounts.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        UserAccount account = new UserAccount();
        account.setFullName(request.fullName().trim());
        account.setEmail(normalizedEmail);
        account.setPhone(request.phone().trim());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AccountRole.VOLUNTEER);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = accounts.save(account);

        VolunteerProfile profile = new VolunteerProfile();
        profile.setUser(account);
        profile.setAddress(request.address() == null ? null : request.address().trim());
        profile.setAvailability(AvailabilityStatus.UNAVAILABLE);
        volunteerProfiles.save(profile);
        return toAuthUser(account);
    }

    @Transactional
    public AuthUser registerRequester(RegisterRequesterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (accounts.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        UserAccount account = new UserAccount();
        account.setFullName(request.fullName().trim());
        account.setEmail(normalizedEmail);
        account.setPhone(request.phone().trim());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AccountRole.USER);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAddress(request.address() == null || request.address().isBlank() ? null : request.address().trim());
        return toAuthUser(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AuthUser findAuthUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        UserAccount account = accounts.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        return toAuthUser(account);
    }

    public AuthUser toAuthUser(UserAccount account) {
        return new AuthUser(account.getId(), account.getFullName(), account.getEmail(), account.getPhone(),
            account.getRole(), account.getAccountStatus(), account.getDepartment(), account.getAddress());
    }
}
