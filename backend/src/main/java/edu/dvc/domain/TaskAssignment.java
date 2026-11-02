package edu.dvc.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_request_volunteer", columnNames = {"request_id", "volunteer_id"}), indexes = @Index(name = "idx_assignment_status", columnList = "status"))
public class TaskAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "request_id", nullable = false)
    private EmergencyRequest request;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "volunteer_id", nullable = false)
    private VolunteerProfile volunteer;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.ASSIGNED;
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    public Long getId() { return id; }
    public EmergencyRequest getRequest() { return request; }
    public void setRequest(EmergencyRequest request) { this.request = request; }
    public VolunteerProfile getVolunteer() { return volunteer; }
    public void setVolunteer(VolunteerProfile volunteer) { this.volunteer = volunteer; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Instant getAssignedAt() { return assignedAt; }
}
