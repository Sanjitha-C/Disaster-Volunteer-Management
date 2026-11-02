package edu.dvc.volunteer;

import java.time.LocalDateTime;

public record AvailabilitySlotDto(Long id, LocalDateTime startsAt, LocalDateTime endsAt) { }
