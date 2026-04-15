package com.songchao.project.modules.projectmgmt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("pm_project")
public class PmProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectCode;
    private String projectName;
    private String projectType;
    private String location;
    private String ownerOrg;
    private BigDecimal contractAmount;
    private LocalDate startDate;
    private LocalDate plannedFinishDate;
    private String projectStatus;
    private String projectManagerName;
    private String projectDesc;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getPlannedFinishDate() { return plannedFinishDate; }
    public void setPlannedFinishDate(LocalDate plannedFinishDate) { this.plannedFinishDate = plannedFinishDate; }
    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public String getProjectManagerName() { return projectManagerName; }
    public void setProjectManagerName(String projectManagerName) { this.projectManagerName = projectManagerName; }
    public String getProjectDesc() { return projectDesc; }
    public void setProjectDesc(String projectDesc) { this.projectDesc = projectDesc; }
}
