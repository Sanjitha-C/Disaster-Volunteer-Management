package edu.dvc.repository;

import edu.dvc.domain.VolunteerSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerSkillRepository extends JpaRepository<VolunteerSkill, Long> {
    List<VolunteerSkill> findByVolunteerId(Long volunteerId);
    boolean existsByVolunteerIdAndSkillId(Long volunteerId, Long skillId);
    boolean existsBySkillId(Long skillId);
}
