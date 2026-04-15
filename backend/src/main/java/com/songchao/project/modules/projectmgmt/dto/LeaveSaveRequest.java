package com.songchao.project.modules.projectmgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeaveSaveRequest {
    @NotBlank
    private String applicantName;
    private Long projectId;
    private String projectName;
    private String projectDeptName;
    @NotBlank
    private String leaveType;
    @NotBlank
    private String startTime;
    @NotBlank
    private String endTime;
    @NotNull
    private Double leaveDays;
    private String reason;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectDeptName() { return projectDeptName; }
    public void setProjectDeptName(String projectDeptName) { this.projectDeptName = projectDeptName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Double getLeaveDays() { return leaveDays; }
    public void setLeaveDays(Double leaveDays) { this.leaveDays = leaveDays; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
