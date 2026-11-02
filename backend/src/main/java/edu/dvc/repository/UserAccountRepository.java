package edu.dvc.repository;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    long countByAccountStatus(AccountStatus accountStatus);
    long countByRoleAndAccountStatus(AccountRole role, AccountStatus accountStatus);
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<UserAccount> findByRoleAndAccountStatus(AccountRole role, AccountStatus accountStatus);
}
