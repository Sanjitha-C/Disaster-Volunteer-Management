package edu.dvc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at"))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "recipient_id", nullable = false)
    private UserAccount recipient;
    @NotBlank @Size(max = 160) @Column(nullable = false, length = 160)
    private String title;
    @NotBlank @Size(max = 2000) @Column(nullable = false, length = 2000)
    private String message;
    @Column(name = "is_read", nullable = false)
    private boolean read;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public UserAccount getRecipient() { return recipient; }
    public void setRecipient(UserAccount recipient) { this.recipient = recipient; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Instant getCreatedAt() { return createdAt; }
}
