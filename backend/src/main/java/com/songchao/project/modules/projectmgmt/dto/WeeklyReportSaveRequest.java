package com.songchao.project.modules.projectmgmt.dto;

import jakarta.validation.constraints.NotBlank;

public class WeeklyReportSaveRequest {
    private Long projectId;
    private String projectName;
    private String projectDeptName;
    @NotBlank
    private String reportUserName;
    @NotBlank
    private String weekLabel;
    private String completedWorkText;
    private String unfinishedWorkText;
    private String unfinishedReasonText;
    private String nextWeekPlanText;
    private String supportNeeds;
    private String remark;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectDeptName() { return projectDeptName; }
    public void setProjectDeptName(String projectDeptName) { this.projectDeptName = projectDeptName; }
    public String getReportUserName() { return reportUserName; }
    public void setReportUserName(String reportUserName) { this.reportUserName = reportUserName; }
    public String getWeekLabel() { return weekLabel; }
    public void setWeekLabel(String weekLabel) { this.weekLabel = weekLabel; }
    public String getCompletedWorkText() { return completedWorkText; }
    public void setCompletedWorkText(String completedWorkText) { this.completedWorkText = completedWorkText; }
    public String getUnfinishedWorkText() { return unfinishedWorkText; }
    public void setUnfinishedWorkText(String unfinishedWorkText) { this.unfinishedWorkText = unfinishedWorkText; }
    public String getUnfinishedReasonText() { return unfinishedReasonText; }
    public void setUnfinishedReasonText(String unfinishedReasonText) { this.unfinishedReasonText = unfinishedReasonText; }
    public String getNextWeekPlanText() { return nextWeekPlanText; }
    public void setNextWeekPlanText(String nextWeekPlanText) { this.nextWeekPlanText = nextWeekPlanText; }
    public String getSupportNeeds() { return supportNeeds; }
    public void setSupportNeeds(String supportNeeds) { this.supportNeeds = supportNeeds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
