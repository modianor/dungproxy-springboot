package com.virjar.dungproxy.server.scheduler.commontask;

import com.virjar.dungproxy.server.model.DomainIpModel;
import com.virjar.dungproxy.server.model.DomainMetaModel;
import com.virjar.dungproxy.server.scheduler.DomainTestTask;
import com.virjar.dungproxy.server.service.DomainIpService;
import com.virjar.dungproxy.server.service.DomainMetaService;
import com.virjar.dungproxy.server.utils.SysConfig;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * 检测DomainIp表中的IP
 * Created by virjar on 16/10/3.
 */
@Component
public class RefreshDomainIpTask extends CommonTask {
    private static final Logger logger = LoggerFactory.getLogger(RefreshDomainIpTask.class);
    private static final String DURATION = "common.task.duration.freshDomainIp";

    @Resource
    private DomainMetaService domainMetaService;
    @Resource
    private DomainIpService domainIpService;
    private Random random = new Random(System.currentTimeMillis());

    public RefreshDomainIpTask() {
        super(NumberUtils.toInt(SysConfig.getInstance().get(DURATION), 176400000));
    }

    /**
     * 为每个domainMate刷新检测domainIp
     * @return
     */
    @Override
    public Object execute() {
        // 查找DomainMeta记录
        List<DomainMetaModel> domainMetaModels = domainMetaService.selectPage(null, null);
        for (DomainMetaModel domainMetaModel : domainMetaModels) {
            DomainIpModel query = new DomainIpModel();
            query.setDomain(domainMetaModel.getDomain());
            // 根据DomainMeta查找DomainIp
            List<DomainIpModel> domainIpModels = domainIpService.selectPage(query, PageRequest.of(0, 5));
            if (domainIpModels.size() < 1) {
                logger.warn("domain:{} has not find domainIps", domainMetaModel.getDomain());
                continue;
            }
            DomainTestTask.sendDomainTask(domainIpModels.get(random.nextInt(domainIpModels.size())).getTestUrl());
        }
        return null;
    }
}
