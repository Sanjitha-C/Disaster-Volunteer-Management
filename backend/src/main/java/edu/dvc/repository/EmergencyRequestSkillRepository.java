package edu.dvc.repository;

import edu.dvc.domain.EmergencyRequestSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyRequestSkillRepository extends JpaRepository<EmergencyRequestSkill, Long> {
    List<EmergencyRequestSkill> findByRequestId(Long requestId);
}
