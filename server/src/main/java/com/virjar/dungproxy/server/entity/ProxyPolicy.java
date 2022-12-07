package com.virjar.dungproxy.server.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 爬虫代理策略对象 proxy_policy
 *
 * @author ruoyi
 * @date 2022-12-05
 */
public class ProxyPolicy {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 爬虫策略ID
     */
    private String policyId;

    /**
     * 代理名称
     */
    private String proxyName;

    /**
     * 代理支持类型
     */
    private String protocol;

    /**
     * 目标站点
     */
    private String sourceHost;

    /**
     * 检测优先级
     */
    private Long priority;

    /**
     * 代理源状态
     */
    private String status;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public Long getPriority() {
        return priority;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("policyId", getPolicyId())
                .append("proxyName", getProxyName())
                .append("protocol", getProtocol())
                .append("sourceHost", getSourceHost())
                .append("priority", getPriority())
                .append("status", getStatus())
                .toString();
    }
}
