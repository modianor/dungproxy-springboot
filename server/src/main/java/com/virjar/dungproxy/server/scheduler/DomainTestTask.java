package com.virjar.dungproxy.server.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.client.util.CommonUtil;
import com.virjar.dungproxy.server.core.beanmapper.BeanMapper;
import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.model.DomainIpModel;
import com.virjar.dungproxy.server.model.DomainMetaModel;
import com.virjar.dungproxy.server.model.ProxyModel;
import com.virjar.dungproxy.server.repository.ProxyRepository;
import com.virjar.dungproxy.server.service.DomainIpService;
import com.virjar.dungproxy.server.service.DomainMetaService;
import com.virjar.dungproxy.server.utils.NameThreadFactory;
import com.virjar.dungproxy.server.utils.ProxyUtil;
import com.virjar.dungproxy.server.utils.SysConfig;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by virjar on 16/9/16.<br/>
 * 针对于指定网站,探测可用资源,探测维度为域名维度,
 */
@Component
public class DomainTestTask implements Runnable, InitializingBean {

    private PriorityQueue<UrlCheckTaskHolder> domainTaskQueue = new PriorityQueue<>();

    private static final Logger logger = LoggerFactory.getLogger(DomainTestTask.class);

    private boolean isRunning = false;

    private ThreadPoolExecutor pool = null;

    private static DomainTestTask instance = null;

    private Set<String> runningDomains = Sets.newConcurrentHashSet();

    @Resource
    private ProxyRepository proxyRepository;

    @Resource
    private DomainIpService domainIpService;

    @Resource
    private DomainMetaService domainMetaService;

    @Resource
    private BeanMapper beanMapper;

    private void init() {
        instance = this;
        isRunning = SysConfig.getInstance().getDomainCheckThread() > 0;
        if (!isRunning) {
            logger.info("domain check task is not running");
            return;
        }
        pool = new ThreadPoolExecutor(SysConfig.getInstance().getDomainCheckThread(), Integer.MAX_VALUE, 30000L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NameThreadFactory("domain-check"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        // 启动当前对象实现的run方法
        new Thread(this).start();
        // 启动URL探测任务
        new OnlyUrlTestTask().start();
        new OnlyUrlTestTask().start();
    }

    private class OnlyUrlTestTask extends Thread {
        public OnlyUrlTestTask() {
            super("OnlyUrlTestTask");
        }

        @Override
        public void run() {
            while (isRunning) {
                // 取出UrlCheckTaskHolder对象
                UrlCheckTaskHolder poll = domainTaskQueue.poll();
                if (poll == null) {
                    CommonUtil.sleep(1000);
                    continue;
                }

                if (poll.url == null) {
                    // 如果是domain检测任务 塞回队列
                    logger.info("get a domain task:{}", poll.domain);
                    domainTaskQueue.offer(poll);
                    CommonUtil.sleep(1000);
                    continue;
                }
                logger.info("get a url test task:{}", poll.url);
                // 这里只处理url探测任务
                new DomainTester(poll.url).call();
            }
        }
    }

    /**
     * 对外暴露任务添加方法 实际上这个方法就是为某个domainMate添加可用domainIp
     *
     * @param url
     * @return
     */
    public static boolean sendDomainTask(final String url) {
        if (instance == null) {
            return false;
        }
        if (!instance.isRunning) {
            logger.info("域名校验组件没有开启,将会把这个任务转发到其他服务器进行处理");
            new Thread() {
                @Override
                public void run() {
                    String s = HttpInvoker.get(SysConfig.getInstance().get("system.domain.test.forward.url"),
                            Lists.<NameValuePair>newArrayList(new BasicNameValuePair("url", url)));
                    logger.info("domain test forward response is {}", s);
                }
            }.start();
            return true;
        }
        // 添加url检测任务
        return instance.addUrlTask(url);
    }

    /**
     * 添加url检测任务方法
     *
     * @param url
     * @return
     */
    public boolean addUrlTask(String url) {
        try {
            // 根据url提取domain
            String domain = CommonUtil.extractDomain(url);
            if (domain == null) {
                return false;
            }
            /**
             * 下面逻辑根据domain去重
             */
            if (runningDomains.contains(domain)) {
                return true;
            } else {
                synchronized (domain.intern()) {
                    if (runningDomains.contains(domain)) {
                        return true;
                    }
                    runningDomains.add(domain);
                }
            }
            UrlCheckTaskHolder urlCheckTaskHolder = new UrlCheckTaskHolder();
            urlCheckTaskHolder.priority = 10;
            urlCheckTaskHolder.url = url;
            urlCheckTaskHolder.domain = domain;
            return domainTaskQueue.offer(urlCheckTaskHolder);
        } catch (Exception e) {
            logger.error("为啥任务仍不进来?", e);
            return false;
        }
    }

    /**
     * 任务处理线程 从domainTaskQueue中获取任务
     * 当获取到的任务为空时,代表任务队列为空,这个时候创建任务,为所有domain下的domainIP进行检测
     * 当获取到的任务不为空时
     * 任务的url为空时,代表这是一个具体的domainMate下domainIp的检测任务
     * 任务的url不为空时,代表这是一个为某个具体domain添加可用domainIp的任务
     */
    @Override
    public void run() {
        List<Future<Object>> futureList = Lists.newArrayList();
        while (isRunning) {
            // 获取检测任务
            UrlCheckTaskHolder holder = domainTaskQueue.poll();
            if (holder == null) {
                // 当检测任务队列为空的时候 检测所有的domainMate
                if (pool.getActiveCount() < pool.getCorePoolSize()) {
                    // 在线程池空闲的时候才加入新任务
                    CommonUtil.waitAllFutures(futureList);
                    futureList.clear();
                    // 当domainTaskQueue任务队列为空状态 所有domain下的domainIP进行检测
                    genHistoryTestTask();
                }
                CommonUtil.sleep(2000);
                continue;
            }
            if (holder.url == null) {
                // 如果检测任务为domainMate检测任务
                // 提交domainMate下所有domainIp的检测
                futureList.add(pool.submit(new HistoryUrlTester(holder.domain)));
            } else {
                // 如果检测任务为proxyIp检测任务
                // 提交proxyIP检测
                futureList.add(pool.submit(new DomainTester(holder.url)));
            }
        }
    }

    /**
     * 为每个domainMate创建检测任务
     */
    private void genHistoryTestTask() {
        // 查找所有的domainMate
        List<DomainMetaModel> domainMetaModels = domainMetaService.selectPage(null, null);
        for (DomainMetaModel domainMetaModel : domainMetaModels) {
            // 为每一个domainMate创建检测任务
            UrlCheckTaskHolder urlCheckTaskHolder = new UrlCheckTaskHolder();
            urlCheckTaskHolder.priority = 1;
            urlCheckTaskHolder.domain = domainMetaModel.getDomain();
            domainTaskQueue.offer(urlCheckTaskHolder);
        }
    }

    /**
     * 封装的任务类,支持优先级
     */
    private class UrlCheckTaskHolder implements Comparable<UrlCheckTaskHolder> {
        int priority;
        String url;
        String domain;

        @Override
        public String toString() {
            return "UrlCheckTaskHolder{" + "domain='" + domain + '\'' + ", url='" + url + '\'' + '}';
        }

        @Override
        public int compareTo(UrlCheckTaskHolder o) {
            return priority - o.priority;
        }
    }

    /**
     * 检测曾经检测过的资源,
     * 检测每个domainMate下所有domainIp的可用性并更新socre
     */
    private class HistoryUrlTester implements Callable<Object> {

        private String domain;

        public HistoryUrlTester(String domain) {
            this.domain = domain;
        }

        @Override
        public Object call() throws Exception {
            try {
                logger.info("domain checker {} is running", domain);
                DomainIpModel queryDomainIpModel = new DomainIpModel();
                queryDomainIpModel.setDomain(domain);
                // 根据domain查询该domain下所有的domainIp
                int total = domainIpService.selectCount(queryDomainIpModel);
                int pageSize = 100;
                // 计算分页场景下页码总数需要这样算,这叫0舍1入。不舍1入?(考虑四舍五入怎么实现)
                int totalPage = (total + pageSize - 1) / pageSize;
                for (int nowPage = 0; nowPage < totalPage; nowPage++) {
                    List<DomainIpModel> domainIpModels = domainIpService.selectPage(queryDomainIpModel,
                            PageRequest.of(nowPage, pageSize));
                    // 分页遍历检测该domain下的domainIp
                    for (DomainIpModel domainIp : domainIpModels) {
                        if (ProxyUtil.checkUrl(domainIp.getIp(), domainIp.getPort(), domainIp.getTestUrl())) {
                            if (domainIp.getDomainScore() < 0) {
                                domainIp.setDomainScore(1L);
                            } else {
                                domainIp.setDomainScore(domainIp.getDomainScore() + 1);
                            }
                        } else {
                            if (domainIp.getDomainScore() > 0) {
                                // 快速降权
                                domainIp.setDomainScore(domainIp.getDomainScore()
                                        - (long) Math.log((double) domainIp.getDomainScore() + 3));
                            } else {
                                domainIp.setDomainScore(domainIp.getDomainScore() - 1);
                            }
                        }
                        domainIp.setDomain(null);
                        domainIp.setIp(null);
                        domainIp.setPort(null);
                        domainIp.setSpeed(null);
                        domainIp.setCreatetime(null);
                        domainIp.setDomainScoreDate(new Date());
                        // 只更新关心的数据,防止并发环境下的各种同步问题,不是数据库同步,而是逻辑层
                        domainIpService.updateByPrimaryKeySelective(domainIp);
                    }
                }
                return null;
            } catch (Exception e) {
                logger.error("error when check domain:{}", domain, e);
                throw e;
            }
        }
    }

    /**
     * 这个任务一个都可能执行几个小时,慢慢等,有耐心,不着急
     * 为每个domain筛选可用domainIp,但是domain是根据testUrl提取的
     */
    private class DomainTester implements Callable<Object> {

        String url;

        DomainTester(String url) {
            this.url = url;
        }

        @Override
        public Object call() {
            try {
                DomainMetaModel domainMetaModel = new DomainMetaModel();
                // 根据testUrl获取domain
                domainMetaModel.setDomain(CommonUtil.extractDomain(url));
                // 检查domainMate库是否存在domain 不存在则创建
                if (domainMetaService.selectCount(domainMetaModel) == 0) {
                    domainMetaService.createSelective(domainMetaModel);
                }
                // 获取proxy表中所有ip 用testUrl对每个ip进行探测可用性
                List<Proxy> available = proxyRepository.findAvailable();// 系统可用IP,根据权值排序
                logger.info("domain check total:{} url:{}", available.size(), url);
                for (Proxy proxy : available) {
                    logger.info("url:{} domain check :{}", url, JSONObject.toJSONString(proxy));
                    if (ProxyUtil.checkUrl(beanMapper.map(proxy, ProxyModel.class), url)) {
                        logger.info("url:{} domain check success :{}", url, JSONObject.toJSONString(proxy));
                        DomainIpModel domainIpModel = new DomainIpModel();
                        domainIpModel.setIp(proxy.getIp());
                        domainIpModel.setPort(proxy.getPort());
                        domainIpModel.setDomain(CommonUtil.extractDomain(url));
                        domainIpModel.setProxyId(proxy.getId());
                        domainIpModel.setTestUrl(url);
                        domainIpModel.setDomainScoreDate(new Date());
                        // 如果可用 将该ip存入domainIp表中
                        domainIpService.create(domainIpModel);
                    } else {
                        logger.warn("url:{} domain check fail :{}", url, JSONObject.toJSONString(proxy));
                    }
                }
                logger.info("check end");
                return null;
            } catch (Throwable e) {
                logger.info("domain check error", e);
            } finally {// 结束后清除锁,允许任务再次触发
                runningDomains.remove(CommonUtil.extractDomain(url));
            }
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
