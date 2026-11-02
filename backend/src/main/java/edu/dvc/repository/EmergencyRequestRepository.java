package edu.dvc.repository;

import edu.dvc.domain.EmergencyRequest;
import edu.dvc.domain.EmergencyRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    long countByStatus(EmergencyRequestStatus status);
    List<EmergencyRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<EmergencyRequest> findByStatusOrderByCreatedAtDesc(EmergencyRequestStatus status);
    List<EmergencyRequest> findAllByOrderByCreatedAtDesc();
}
