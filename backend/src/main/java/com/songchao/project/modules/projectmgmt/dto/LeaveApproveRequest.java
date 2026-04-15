package com.songchao.project.modules.projectmgmt.dto;

import jakarta.validation.constraints.NotBlank;

public class LeaveApproveRequest {
    @NotBlank
    private String action;
    private String comment;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
