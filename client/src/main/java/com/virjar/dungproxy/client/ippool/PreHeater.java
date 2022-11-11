
package com.virjar.dungproxy.client.ippool;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.virjar.dungproxy.client.ippool.config.DungProxyContext;
import com.virjar.dungproxy.client.ippool.config.ObjectFactory;
import com.virjar.dungproxy.client.ippool.strategy.ProxyChecker;
import com.virjar.dungproxy.client.ippool.strategy.ResourceFacade;
import com.virjar.dungproxy.client.model.AvProxy;
import com.virjar.dungproxy.client.model.AvProxyVO;
import com.virjar.dungproxy.client.util.CommonUtil;

/**
 * Description: 初始化时加载Proxy 定时收集Proxy<br/>
 * 一个工具类,离线跑代理IP数据,构建契合本地环境的代理IP数据
 *
 * @author lingtong.fu
 * @version 2016-09-11 18:16
 */

public class PreHeater {

    private static final Logger logger = LoggerFactory.getLogger(PreHeater.class);
    private Set<String> taskUrls = Sets.newConcurrentHashSet();
    private int threadNumber = 40;// TODO 配置这个参数
    private ExecutorService pool;
    private AtomicBoolean hasInit = new AtomicBoolean(false);
    private Map<String, DomainPool> stringDomainPoolMap;
    private AtomicLong passedProxyNumber = new AtomicLong(0);
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private DungProxyContext dungProxyContext;

    public PreHeater(DungProxyContext dungProxyContext) {
        this.dungProxyContext = dungProxyContext;
    }

    /**
     * 提供一个主函数,用于在jar包调用preHeater模块,为数据真正可用做准备
     */
    public static void main(String[] args) {
        start();
    }

    /**
     * 老代码兼容,设计所以改成静态方法
     */
    public static void start() {
        PreHeater preHeater = DungProxyContext.create().buildDefaultConfigFile().handleConfig().getPreHeater();
        preHeater.doPreHeat();
        preHeater.destroy();
    }

    private void init() {
        if (hasInit.compareAndSet(false, true)) {
            pool = Executors.newFixedThreadPool(threadNumber);
            isRunning.set(true);
            AvProxy.needRecordChange = false;
            try {
                unSerialize();
            } finally {
                AvProxy.needRecordChange = true;
            }
        }
    }

    public void destroy() {
        isRunning.set(false);
        pool.shutdown();
    }

    public PreHeater addTask(String url) {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        // TODO 中文路径和中文参数处理
        taskUrls.add(url);
        return this;
    }

    public synchronized void doPreHeat() {
        AvProxy.needRecordChange = false;
        if (taskUrls.size() == 0) {
            logger.warn("preHeater task is empty");
            return;
        }
        if (!hasInit.get()) {
            init();
        }
        logger.info("待测试任务:{}", JSONArray.toJSONString(taskUrls));
        Map<String, String> urlMap = transformDomainUrlMap(taskUrls);
        List<Future<Boolean>> futureList = Lists.newArrayList();
        ResourceFacade resourceFacade = ObjectFactory.newInstance(dungProxyContext.getDefaultResourceFacade());// 这里使用全局IP下载器
        logger.info("下载可用IP...");
        List<AvProxyVO> candidateProxies = resourceFacade.allAvailable();
        logger.info("总共下载到{}个IP资源", candidateProxies.size());
        // 加载历史数据
        for (Map.Entry<String, DomainPool> entry : stringDomainPoolMap.entrySet()) {
            if (!urlMap.containsKey(entry.getKey())) {
                continue;
            }
            Iterator<AvProxy> iterator = entry.getValue().availableProxy().iterator();
            while (iterator.hasNext()) {
                AvProxy avProxy = iterator.next();
                futureList.add(pool.submit(new UrlCheckTask(AvProxyVO.fromModel(avProxy), urlMap.get(entry.getKey()))));
                iterator.remove();
            }
        }

        // 加载服务器新导入的资源
        for (AvProxyVO avProxy : candidateProxies) {
            for (String url : taskUrls) {
                futureList.add(pool.submit(new UrlCheckTask(avProxy, url)));
            }
        }
        CommonUtil.waitAllFutures(futureList);
        dungProxyContext.getAvProxyDumper().serializeProxy(getPoolInfo(stringDomainPoolMap));
    }

    private Map<String, String> transformDomainUrlMap(Collection<String> testUrls) {
        Map<String, String> ret = Maps.newHashMap();
        for (String url : testUrls) {
            ret.put(CommonUtil.extractDomain(url), url);
        }
        return ret;
    }

    private class UrlCheckTask implements Callable<Boolean> {
        String url;
        AvProxyVO proxy;

        UrlCheckTask(AvProxyVO proxy, String url) {
            this.proxy = proxy;
            this.url = url;
        }

        @Override
        public Boolean call() throws Exception {
            String domain = CommonUtil.extractDomain(url);
            // 路由一波,保证和api规则一直
            domain = dungProxyContext.getGroupBindRouter().routeDomain(domain);

            DomainPool domainPool = stringDomainPoolMap.get(domain);
            if (domainPool == null) {
                synchronized (UrlCheckTask.class) {
                    domainPool = stringDomainPoolMap.get(domain);
                    if (domainPool == null) {
                        domainPool = new DomainPool(domain, dungProxyContext.genDomainContext(domain));
                        stringDomainPoolMap.put(domain, domainPool);
                    }
                }
            }
            ProxyChecker proxyChecker = dungProxyContext.genDomainContext(domain).getProxyChecker();
            if (proxyChecker.available(proxy, url)) {
                domainPool.addAvailable(proxy.toModel(domainPool));
                logger.info("preHeater available test passed for proxy:{} for url:{}", JSONObject.toJSONString(proxy),
                        url);

                if (passedProxyNumber.incrementAndGet() % dungProxyContext.getSerializeStep() == 0) {// 预热的时候,每产生20个IP,就序列化一次数据。
                    dungProxyContext.getAvProxyDumper().serializeProxy(getPoolInfo(stringDomainPoolMap));
                }
                return true;
            } else {
                return false;

            }
        }
    }

    public PreHeater setThreadNumber(int threadNumber) {
        if (threadNumber < 1) {
            threadNumber = 1;
        }
        this.threadNumber = threadNumber;
        return this;
    }

    private void unSerialize() {
        final Map<String, DomainPool> pool = Maps.newConcurrentMap();
        stringDomainPoolMap = pool;
        Map<String, List<AvProxyVO>> stringListMap = dungProxyContext.getAvProxyDumper().unSerializeProxy();
        if (stringListMap == null) {
            return;
        }

        for (final Map.Entry<String, List<AvProxyVO>> entry : stringListMap.entrySet()) {
            List<AvProxy> avProxies = Lists.transform(entry.getValue(), new Function<AvProxyVO, AvProxy>() {
                @Override
                public AvProxy apply(AvProxyVO input) {
                    return input.toModel(dungProxyContext.genDomainContext(entry.getKey()));
                }
            });

            if (!pool.containsKey(entry.getKey())) {
                pool.put(entry.getKey(),
                        new DomainPool(entry.getKey(), dungProxyContext.genDomainContext(entry.getKey())));
            }
            pool.get(entry.getKey()).addAvailable(avProxies);
        }
        stringDomainPoolMap = pool;
    }

    private Map<String, List<AvProxyVO>> getPoolInfo(Map<String, DomainPool> pool) {
        return Maps.transformValues(pool, new Function<DomainPool, List<AvProxyVO>>() {
            @Override
            public List<AvProxyVO> apply(DomainPool domainPool) {// copy 一份新数据出去,数据结构会给外部使用,随意暴露可能会导致数据错误
                return Lists.transform(domainPool.availableProxy(), new Function<AvProxy, AvProxyVO>() {
                    @Override
                    public AvProxyVO apply(AvProxy input) {
                        return AvProxyVO.fromModel(input);
                    }
                });

            }
        });
    }
}
