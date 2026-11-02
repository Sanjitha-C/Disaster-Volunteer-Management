package edu.dvc.volunteer;

import edu.dvc.domain.AvailabilityStatus;

public record VolunteerProfileDto(Long id, String fullName, String email, String phone, String address,
                                  AvailabilityStatus availability, int profileCompletionPercent) { }
