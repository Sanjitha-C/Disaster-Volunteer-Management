package edu.dvc.operations;

import java.time.Instant;

public record NotificationDto(Long id, String title, String message, boolean read, Instant createdAt) {}
