package com.songchao.project.modules.projectmgmt.dto;

public class TaskSaveRequest {
    private Long projectId;
    private Long projectDeptId;
    private Long assigneeId;
    private String projectName;
    private String projectDeptName;
    private String taskTitle;
    private String assigneeName;
    private String priority;
    private String requiredFinishDate;
    private String taskContent;
    private String remark;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectDeptId() { return projectDeptId; }
    public void setProjectDeptId(Long projectDeptId) { this.projectDeptId = projectDeptId; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectDeptName() { return projectDeptName; }
    public void setProjectDeptName(String projectDeptName) { this.projectDeptName = projectDeptName; }
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRequiredFinishDate() { return requiredFinishDate; }
    public void setRequiredFinishDate(String requiredFinishDate) { this.requiredFinishDate = requiredFinishDate; }
    public String getTaskContent() { return taskContent; }
    public void setTaskContent(String taskContent) { this.taskContent = taskContent; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
