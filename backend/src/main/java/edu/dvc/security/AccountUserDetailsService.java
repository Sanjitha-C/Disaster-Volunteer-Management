package edu.dvc.security;

import edu.dvc.domain.UserAccount;
import edu.dvc.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {
    private final UserAccountRepository accounts;

    public AccountUserDetailsService(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount account = accounts.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
        return User.withUsername(account.getEmail())
            .password(account.getPasswordHash())
            .roles(account.getRole().name())
            .disabled(account.getAccountStatus() != edu.dvc.domain.AccountStatus.ACTIVE)
            .build();
    }
}
