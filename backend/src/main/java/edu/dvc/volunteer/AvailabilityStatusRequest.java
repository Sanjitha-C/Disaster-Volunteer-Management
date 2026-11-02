package edu.dvc.volunteer;

import edu.dvc.domain.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public record AvailabilityStatusRequest(@NotNull AvailabilityStatus status) { }
