package edu.dvc.operations;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import edu.dvc.domain.ResourceStatus;

public record ResourceRequest(@NotBlank @Size(max = 120) String name, @Min(0) int quantity,
                              @NotBlank @Size(max = 32) String unit, @NotNull ResourceStatus status) {}
