package edu.dvc.admin;

import java.util.List;

public record DashboardReportDto(long activeUsers, long activeVolunteers, long availableVolunteers,
        long pendingRequests, long openAssignments, long completedRequests, long availableResources,
        double averageFeedbackRating, long totalFeedback, List<CountDto> requestStatuses,
        List<CountDto> assignmentStatuses) {
    public record CountDto(String label, long count) {}
}
