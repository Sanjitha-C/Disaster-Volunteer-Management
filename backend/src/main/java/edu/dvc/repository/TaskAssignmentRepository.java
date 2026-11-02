package edu.dvc.repository;

import edu.dvc.domain.TaskAssignment;
import edu.dvc.domain.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    long countByStatus(TaskStatus status);
    List<TaskAssignment> findByVolunteerIdOrderByAssignedAtDesc(Long volunteerId);
    List<TaskAssignment> findByRequestIdOrderByAssignedAtDesc(Long requestId);
    List<TaskAssignment> findByStatus(TaskStatus status);
    boolean existsByRequestIdAndVolunteerId(Long requestId, Long volunteerId);
}
