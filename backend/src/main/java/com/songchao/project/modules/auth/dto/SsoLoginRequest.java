package com.songchao.project.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class SsoLoginRequest {
    @NotBlank
    private String ticket;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}

