package com.eventpass.backend.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalOrganizers;
    private long pendingOrganizerRequests;
    private long totalEvents;
    private long activeEvents;
    private long totalTicketsSold;
    private long totalRevenue;
}