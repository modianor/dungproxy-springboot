package com.virjar.dungproxy.server.scheduler.commontask;

import com.virjar.dungproxy.server.repository.ProxyRepository;
import com.virjar.dungproxy.server.service.DomainIpService;
import com.virjar.dungproxy.server.service.ProxyService;
import com.virjar.dungproxy.server.utils.SysConfig;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 下线域名IP池数据 Created by virjar on 16/10/3.
 */
@Component
public class OfflineProxyIpTask extends CommonTask {
    private static final Logger logger = LoggerFactory.getLogger(OfflineProxyIpTask.class);
    private static final String DURATION = "common.task.duration.offlineProxyIp";

    @Resource
    private ProxyService proxyService;

    public OfflineProxyIpTask() {
        super(NumberUtils.toInt(SysConfig.getInstance().get(DURATION), 10800000));
    }

    @Override
    public Object execute() {
        proxyService.offline();
        return "";
    }
}
