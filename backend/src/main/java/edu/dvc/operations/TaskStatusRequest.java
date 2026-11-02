package edu.dvc.operations;

import edu.dvc.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(@NotNull TaskStatus status) {}
