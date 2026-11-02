package edu.dvc.volunteer;

import edu.dvc.domain.AvailabilityStatus;
import java.util.List;

public record AvailabilityResponse(AvailabilityStatus status, List<AvailabilitySlotDto> slots) { }
