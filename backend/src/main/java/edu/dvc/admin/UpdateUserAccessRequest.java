package edu.dvc.admin;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserAccessRequest(@NotNull AccountRole role, @NotNull AccountStatus accountStatus) {}
