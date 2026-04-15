package com.songchao.project.security.sso;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portal")
public class PortalSsoProperties {
    private String ssoBaseUrl;
    private String appCode;

    public String getSsoBaseUrl() {
        return ssoBaseUrl;
    }

    public void setSsoBaseUrl(String ssoBaseUrl) {
        this.ssoBaseUrl = ssoBaseUrl;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}

