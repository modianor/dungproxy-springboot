package com.virjar.dungproxy.server.vo;

import com.virjar.dungproxy.client.model.AvProxy;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by virjar on 16/10/3.
 */
public class FeedBackForm {
    @NotEmpty
    private String domain;

    @NotNull
    private List<AvProxy> avProxy;
    @NotNull
    private List<AvProxy> disableProxy;

    public List<AvProxy> getAvProxy() {
        return avProxy;
    }

    public void setAvProxy(List<AvProxy> avProxy) {
        this.avProxy = avProxy;
    }

    public List<AvProxy> getDisableProxy() {
        return disableProxy;
    }

    public void setDisableProxy(List<AvProxy> disableProxy) {
        this.disableProxy = disableProxy;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
