package edu.dvc.coordination;

import edu.dvc.domain.EmergencyRequestStatus;
import edu.dvc.domain.EmergencyUrgency;
import java.time.Instant;
import java.util.List;

public record EmergencyRequestDto(Long id, String title, String description, String location,
                                  EmergencyUrgency urgency, EmergencyRequestStatus status,
                                  Instant createdAt, String requesterName, String requesterEmail,
                                  List<RequestSkillDto> requiredSkills, List<AssignmentDto> assignments) {}
