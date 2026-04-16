package com.songchao.project.modules.projectmgmt.dto;

public class ProjectMemberSaveRequest {
    private Long projectId;
    private String projectDeptName;
    private String employeeName;
    private String positionName;
    private String arrivalDate;
    private String leaveDate;
    private Integer onDuty;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectDeptName() { return projectDeptName; }
    public void setProjectDeptName(String projectDeptName) { this.projectDeptName = projectDeptName; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }
    public String getLeaveDate() { return leaveDate; }
    public void setLeaveDate(String leaveDate) { this.leaveDate = leaveDate; }
    public Integer getOnDuty() { return onDuty; }
    public void setOnDuty(Integer onDuty) { this.onDuty = onDuty; }
}
