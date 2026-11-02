package edu.dvc.operations;

import edu.dvc.domain.ResourceStatus;

public record ResourceDto(Long id, String name, int quantity, String unit, ResourceStatus status) {}
