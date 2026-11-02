package edu.dvc.operations;

import java.time.Instant;

public record FeedbackDto(Long id, Long assignmentId, Long volunteerId, String volunteerName,
                          String requestTitle, int rating, String comments, Instant createdAt) {}
