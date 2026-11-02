package edu.dvc.volunteer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateVolunteerProfileRequest(
    @NotBlank @Size(max = 120) String fullName,
    @NotBlank @Pattern(regexp = "^[+0-9() -]{7,20}$") String phone,
    @Size(max = 500) String address
) { }
