package edu.dvc.admin;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import java.time.Instant;

public record AdminUserDto(Long id, String fullName, String email, String phone, String department,
                           AccountRole role, AccountStatus accountStatus, Instant createdAt) {}
