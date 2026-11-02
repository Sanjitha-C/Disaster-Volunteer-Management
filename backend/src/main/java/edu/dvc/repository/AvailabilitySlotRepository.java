package edu.dvc.repository;

import edu.dvc.domain.AvailabilitySlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByVolunteerIdOrderByStartsAt(Long volunteerId);
    boolean existsByVolunteerIdAndStartsAtLessThanAndEndsAtGreaterThan(Long volunteerId, java.time.LocalDateTime endsAt, java.time.LocalDateTime startsAt);
    boolean existsByVolunteerIdAndStartsAtLessThanAndEndsAtGreaterThanAndIdNot(Long volunteerId, java.time.LocalDateTime endsAt, java.time.LocalDateTime startsAt, Long id);
}
