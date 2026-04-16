package com.songchao.project.modules.projectmgmt.dto;

public class ProjectSaveRequest {
    private String projectCode;
    private String projectName;
    private String projectType;
    private String location;
    private String ownerOrg;
    private String contractAmount;
    private String startDate;
    private String plannedFinishDate;
    private String projectStatus;
    private String projectManagerName;
    private String projectDesc;

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getOwnerOrg() { return ownerOrg; }
    public void setOwnerOrg(String ownerOrg) { this.ownerOrg = ownerOrg; }
    public String getContractAmount() { return contractAmount; }
    public void setContractAmount(String contractAmount) { this.contractAmount = contractAmount; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getPlannedFinishDate() { return plannedFinishDate; }
    public void setPlannedFinishDate(String plannedFinishDate) { this.plannedFinishDate = plannedFinishDate; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public String getProjectManagerName() { return projectManagerName; }
    public void setProjectManagerName(String projectManagerName) { this.projectManagerName = projectManagerName; }
    public String getProjectDesc() { return projectDesc; }
    public void setProjectDesc(String projectDesc) { this.projectDesc = projectDesc; }
}
