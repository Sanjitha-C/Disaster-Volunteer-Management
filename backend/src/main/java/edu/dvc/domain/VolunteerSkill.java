package edu.dvc.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "volunteer_skills", uniqueConstraints = @UniqueConstraint(name = "uk_volunteer_skill", columnNames = {"volunteer_id", "skill_id"}))
public class VolunteerSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "volunteer_id", nullable = false)
    private VolunteerProfile volunteer;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public Long getId() { return id; }
    public VolunteerProfile getVolunteer() { return volunteer; }
    public void setVolunteer(VolunteerProfile volunteer) { this.volunteer = volunteer; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
}
