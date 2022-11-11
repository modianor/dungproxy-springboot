package com.virjar.dungproxy.server.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainMetaVO {
    private Long id;

    private String domain;

    private Boolean isForeign;

    private Boolean isDomestic;

    private Boolean isAllowLostHeader;

    private Boolean hasHttps;

    private String supportIsp;
    private Date lastAccessTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getIsForeign() {
        return isForeign;
    }

    public void setIsForeign(Boolean isForeign) {
        this.isForeign = isForeign;
    }

    public Boolean getIsDomestic() {
        return isDomestic;
    }

    public void setIsDomestic(Boolean isDomestic) {
        this.isDomestic = isDomestic;
    }

    public Boolean getIsAllowLostHeader() {
        return isAllowLostHeader;
    }

    public void setIsAllowLostHeader(Boolean isAllowLostHeader) {
        this.isAllowLostHeader = isAllowLostHeader;
    }

    public Boolean getHasHttps() {
        return hasHttps;
    }

    public void setHasHttps(Boolean hasHttps) {
        this.hasHttps = hasHttps;
    }

    public String getSupportIsp() {
        return supportIsp;
    }

    public void setSupportIsp(String supportIsp) {
        this.supportIsp = supportIsp;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}