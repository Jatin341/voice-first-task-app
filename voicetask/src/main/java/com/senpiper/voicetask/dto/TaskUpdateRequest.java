package com.senpiper.voicetask.dto;

import com.senpiper.voicetask.model.TaskStatus;
import lombok.Data;

@Data
public class TaskUpdateRequest {
    private TaskStatus status;
}