package com.virjar.dungproxy.server.entity;

/**
 * 数据源，通过特定url加载代理资源列表，HA资源路由过程发生在固定source下，不会发生source之间的资源飘逸
 */
public class Source {
    private String id;
    private String type;
    private String name;
    private String protocol;
    private String sourceUrl;
    private String mappingSpace;
    private String upstreamAuthUser;
    private String upstreamAuthPassword;
    private String status;

    public Source() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getMappingSpace() {
        return mappingSpace;
    }

    public void setMappingSpace(String mappingSpace) {
        this.mappingSpace = mappingSpace;
    }

    public String getUpstreamAuthUser() {
        return upstreamAuthUser;
    }

    public void setUpstreamAuthUser(String upstreamAuthUser) {
        this.upstreamAuthUser = upstreamAuthUser;
    }

    public String getUpstreamAuthPassword() {
        return upstreamAuthPassword;
    }

    public void setUpstreamAuthPassword(String upstreamAuthPassword) {
        this.upstreamAuthPassword = upstreamAuthPassword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Source{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", protocol='" + protocol + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", mappingSpace='" + mappingSpace + '\'' +
                ", upstreamAuthUser='" + upstreamAuthUser + '\'' +
                ", upstreamAuthPassword='" + upstreamAuthPassword + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
