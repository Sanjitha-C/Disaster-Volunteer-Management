package edu.dvc.auth;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;

public record AuthUser(Long id, String fullName, String email, String phone, AccountRole role,
                       AccountStatus accountStatus, String department, String address) { }
