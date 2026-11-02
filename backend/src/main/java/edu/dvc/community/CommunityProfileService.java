package edu.dvc.community;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.UserAccount;
import edu.dvc.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityProfileService {
    private final UserAccountRepository accounts;

    public CommunityProfileService(UserAccountRepository accounts) { this.accounts = accounts; }

    @Transactional(readOnly = true)
    public CommunityProfileDto getProfile(String email) { return toDto(requireRequester(email)); }

    @Transactional(readOnly = true)
    public CommunityProfileDto getAccountProfile(String email) { return toDto(findAccount(email)); }

    @Transactional
    public CommunityProfileDto updateProfile(String email, UpdateCommunityProfileRequest request) {
        UserAccount account = requireRequester(email);
        apply(account, request);
        return toDto(account);
    }

    @Transactional
    public CommunityProfileDto updateAccountProfile(String currentEmail, UpdateCommunityProfileRequest request) {
        UserAccount account = findAccount(currentEmail);
        apply(account, request);
        return toDto(account);
    }

    private void apply(UserAccount account, UpdateCommunityProfileRequest request) {
        String newEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (!account.getEmail().equalsIgnoreCase(newEmail) && accounts.existsByEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        account.setFullName(request.fullName().trim());
        account.setEmail(newEmail);
        account.setPhone(request.phone().trim());
        account.setAddress(request.address() == null || request.address().isBlank() ? null : request.address().trim());
    }

    private UserAccount requireRequester(String email) {
        UserAccount account = findAccount(email);
        if (account.getRole() != AccountRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Community requester account required");
        }
        return account;
    }

    private UserAccount findAccount(String email) {
        return accounts.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private CommunityProfileDto toDto(UserAccount account) {
        return new CommunityProfileDto(account.getFullName(), account.getEmail(), account.getPhone(), account.getAddress());
    }
}
