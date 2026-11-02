package edu.dvc.coordination;

import edu.dvc.domain.EmergencyUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EmergencyRequestCreateRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 5000) String description,
    @NotBlank @Size(max = 255) String location,
    @NotNull EmergencyUrgency urgency,
    @Size(max = 12) List<@NotBlank @Size(max = 80) String> requiredSkills
) {}
