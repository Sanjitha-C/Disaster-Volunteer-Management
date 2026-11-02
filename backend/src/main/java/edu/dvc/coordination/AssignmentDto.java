package edu.dvc.coordination;

import edu.dvc.domain.TaskStatus;
import java.time.Instant;

public record AssignmentDto(Long id, Long volunteerId, String volunteerName, String volunteerEmail,
                            TaskStatus status, Instant assignedAt) {}
