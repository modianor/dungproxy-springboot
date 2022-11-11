package com.virjar.dungproxy.client.ippool.support.http;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.virjar.dungproxy.client.VERSION;
import com.virjar.dungproxy.client.ippool.DomainPool;
import com.virjar.dungproxy.client.ippool.IpPool;
import com.virjar.dungproxy.client.ippool.SmartProxyQueue;
import com.virjar.dungproxy.client.ippool.config.ProxyConstant;
import com.virjar.dungproxy.client.util.CommonUtil;

/**
 * 用于在线配置和监控IP池的状态</br>
 * Created by virjar on 16/12/3.
 */
public class PoolManager {
    public static final PoolManager instance = new PoolManager();
    private IpPool ipPool = IpPool.getInstance();

    public Map<String, Object> returnJSONBasicStat() {
        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("Version", VERSION.getVersionNumber());
        dataMap.put("JavaVMName", System.getProperty("java.vm.name"));
        dataMap.put("JavaVersion", System.getProperty("java.version"));
        dataMap.put("JavaClassPath", System.getProperty("java.class.path"));
        dataMap.put("StartTime", CommonUtil.toString(CommonUtil.getStartTime()));
        URL resource = PoolManager.class.getClassLoader().getResource(ProxyConstant.CLIENT_CONFIG_FILE_NAME);
        if (resource != null) {
            dataMap.put("configPath", resource.getFile());
        } else {
            dataMap.put("configPath", "not config ,ip pool not enable");
        }
        dataMap.put("domainNum", ipPool.totalDomain());
        return dataMap;
    }

    public List<Map<String, String>> domains() {
        Map<String, DomainPool> pool = ipPool.getPool();
        List<Map<String, String>> ret = Lists.newArrayList();
        for (Map.Entry<String, DomainPool> entry : pool.entrySet()) {
            DomainPool domainPool = entry.getValue();
            Map<String, String> record = Maps.newHashMap();
            record.put("domain", entry.getKey());
            record.put("coreSize", String.valueOf(domainPool.getCoreSize()));
            record.put("size", String.valueOf(domainPool.getSmartProxyQueue().allSize()));
            record.put("minSize", "本字段废弃");
            ret.add(record);
        }
        return ret;
    }

    // 获取某个domain的信息
    public Map<String, Object> domainInfo(String domain) {
        Map<String, Object> ret = Maps.newHashMap();
        DomainPool domainPool = ipPool.getPool().get(domain);
        ret.put("domain", domain);
        ret.put("coreSize", domainPool.getCoreSize());
        ret.put("isRefreshing", domainPool.isRefreshing());
        ret.put("site", domainPool.getSmartProxyQueue().allSize());
        // 容器相关信息
        SmartProxyQueue smartProxyQueue = domainPool.getSmartProxyQueue();
        ret.put("queue_ratio", smartProxyQueue.getRatio());
        ret.put("queue_proxies", Lists.newArrayList(smartProxyQueue.values()));

        return ret;
    }
}
