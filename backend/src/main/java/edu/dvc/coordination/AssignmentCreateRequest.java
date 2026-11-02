package edu.dvc.coordination;

import jakarta.validation.constraints.NotNull;

public record AssignmentCreateRequest(@NotNull Long volunteerId) {}
