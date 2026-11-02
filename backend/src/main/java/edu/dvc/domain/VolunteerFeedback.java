package edu.dvc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "volunteer_feedback", uniqueConstraints = @UniqueConstraint(name = "uk_feedback_assignment", columnNames = "assignment_id"))
public class VolunteerFeedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "assignment_id", nullable = false, unique = true)
    private TaskAssignment assignment;
    @Min(1) @Max(5) @Column(nullable = false)
    private int rating;
    @Size(max = 2000) @Column(length = 2000)
    private String comments;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public TaskAssignment getAssignment() { return assignment; }
    public void setAssignment(TaskAssignment assignment) { this.assignment = assignment; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Instant getCreatedAt() { return createdAt; }
}
