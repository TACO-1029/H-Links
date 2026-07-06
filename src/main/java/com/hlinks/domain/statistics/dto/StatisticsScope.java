package com.hlinks.domain.statistics.dto;

public record StatisticsScope(
        StatisticsViewerType viewerType,
        Long viewerUserId,
        Long departmentId,
        boolean includeChildDepartments
) {

    public static StatisticsScope hrAdmin() {
        return new StatisticsScope(StatisticsViewerType.HR_ADMIN, null, null, true);
    }

    public static StatisticsScope teamLeader(Long viewerUserId, Long departmentId) {
        return new StatisticsScope(StatisticsViewerType.TEAM_LEADER, viewerUserId, departmentId, false);
    }
}
