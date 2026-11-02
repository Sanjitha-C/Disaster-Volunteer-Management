package edu.dvc.repository;

import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.VolunteerProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, Long> {
    Optional<VolunteerProfile> findByUserId(Long userId);
    List<VolunteerProfile> findByAvailability(AvailabilityStatus availability);
}
