package edu.dvc.repository;

import edu.dvc.domain.VolunteerFeedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerFeedbackRepository extends JpaRepository<VolunteerFeedback, Long> {
    List<VolunteerFeedback> findByAssignmentVolunteerIdOrderByCreatedAtDesc(Long volunteerId);
    List<VolunteerFeedback> findAllByOrderByCreatedAtDesc();
    boolean existsByAssignmentId(Long assignmentId);
}
