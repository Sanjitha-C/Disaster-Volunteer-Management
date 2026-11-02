package edu.dvc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "emergency_requests", indexes = {
    @Index(name = "idx_request_status_urgency", columnList = "status, urgency"),
    @Index(name = "idx_request_created_at", columnList = "created_at")
})
public class EmergencyRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "requester_id", nullable = false)
    private UserAccount requester;
    @NotBlank @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String title;
    @NotBlank @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;
    @NotBlank @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EmergencyUrgency urgency;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private EmergencyRequestStatus status = EmergencyRequestStatus.PENDING;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public UserAccount getRequester() { return requester; }
    public void setRequester(UserAccount requester) { this.requester = requester; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public EmergencyUrgency getUrgency() { return urgency; }
    public void setUrgency(EmergencyUrgency urgency) { this.urgency = urgency; }
    public EmergencyRequestStatus getStatus() { return status; }
    public void setStatus(EmergencyRequestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
