package edu.dvc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDateTime;

@Entity
@Table(name = "availability_slots", indexes = @Index(name = "idx_availability_volunteer_start", columnList = "volunteer_id, starts_at"))
public class AvailabilitySlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "volunteer_id", nullable = false)
    private VolunteerProfile volunteer;
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;
    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @AssertTrue(message = "Availability end must be after its start")
    public boolean isTimeRangeValid() { return startsAt == null || endsAt == null || endsAt.isAfter(startsAt); }
    public Long getId() { return id; }
    public VolunteerProfile getVolunteer() { return volunteer; }
    public void setVolunteer(VolunteerProfile volunteer) { this.volunteer = volunteer; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
}
