package edu.dvc.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "emergency_request_skills", uniqueConstraints = @UniqueConstraint(name = "uk_request_skill", columnNames = {"request_id", "skill_id"}))
public class EmergencyRequestSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "request_id", nullable = false)
    private EmergencyRequest request;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public Long getId() { return id; }
    public EmergencyRequest getRequest() { return request; }
    public void setRequest(EmergencyRequest request) { this.request = request; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
}
