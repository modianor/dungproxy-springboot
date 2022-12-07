package com.virjar.dungproxy.server.scheduler.commontask;

import com.virjar.dungproxy.server.entity.ProxyPolicy;
import com.virjar.dungproxy.server.scheduler.DomainTestTask;
import com.virjar.dungproxy.server.service.ProxyPolicyService;
import com.virjar.dungproxy.server.utils.SysConfig;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 下线域名IP池数据 Created by virjar on 16/10/3.
 */
@Component
public class ProxyPolicyTask extends CommonTask {
    private static final Logger logger = LoggerFactory.getLogger(ProxyPolicyTask.class);
    private static final String DURATION = "common.task.duration.proxyPolicyTask";

    @Resource
    private ProxyPolicyService proxyPolicyService;

    public ProxyPolicyTask() {
        super(NumberUtils.toInt(SysConfig.getInstance().get(DURATION), 10800000));
    }

    @Override
    public Object execute() {
        ProxyPolicy proxyPolicy = new ProxyPolicy();
        proxyPolicy.setStatus("0");
        List<ProxyPolicy> proxyPolicies = proxyPolicyService.selectProxyPolicyList(proxyPolicy);
        for (ProxyPolicy policy : proxyPolicies) {
            boolean status = DomainTestTask.sendProxyPolicyTask(policy);
            if (status) {
                logger.info("ProxyPolicyTask: {} success...", policy.toString());
            } else {
                logger.warn("ProxyPolicyTask: {} fail...", policy.toString());
            }
        }
        return "";
    }
}
