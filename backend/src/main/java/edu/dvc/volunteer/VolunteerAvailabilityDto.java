package edu.dvc.volunteer;

import edu.dvc.domain.AvailabilityStatus;
import java.util.List;

public record VolunteerAvailabilityDto(Long volunteerId, String fullName, String email, String phone,
                                       String address, AvailabilityStatus status,
                                       List<AvailabilitySlotDto> slots) { }
