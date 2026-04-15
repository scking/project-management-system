package com.songchao.project.modules.projectmgmt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;

@TableName("pm_weekly_report")
public class PmWeeklyReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportCode;
    private Long projectId;
    private String projectName;
    private String projectDeptName;
    private String reportUserName;
    private String weekLabel;
    private LocalDate reportDate;
    private String completedWorkText;
    private String unfinishedWorkText;
    private String unfinishedReasonText;
    private String nextWeekPlanText;
    private String supportNeeds;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
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
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
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
