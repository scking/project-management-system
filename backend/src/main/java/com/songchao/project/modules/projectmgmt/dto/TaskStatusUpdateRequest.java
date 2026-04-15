package com.songchao.project.modules.projectmgmt.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskStatusUpdateRequest {
    @NotBlank
    private String taskStatus;

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
}
