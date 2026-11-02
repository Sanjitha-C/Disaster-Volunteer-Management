package edu.dvc.volunteer;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AvailabilitySlotRequest(@NotNull @Future LocalDateTime startsAt,
                                      @NotNull @Future LocalDateTime endsAt) {
    @AssertTrue(message = "Availability end must be after its start")
    public boolean isTimeRangeValid() {
        return startsAt == null || endsAt == null || endsAt.isAfter(startsAt);
    }
}
