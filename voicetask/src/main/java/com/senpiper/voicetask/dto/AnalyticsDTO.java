package com.senpiper.voicetask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class AnalyticsDTO {
    private long totalTasks;
    private long completedOnTime;
    private long pending;
    private long delayed;
    private long cancelled;
    private double completionRate;
    private Map<String, Long> weeklyCreated;
}