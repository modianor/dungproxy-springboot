package com.virjar.dungproxy.server.scheduler.commontask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.repository.ProxyRepository;
import com.virjar.dungproxy.server.utils.SysConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nicholas on 8/14/2016.
 */
@Component
public class TaobaoAreaTask extends CommonTask {

    private static final String TAOBAOURL = "https://ip.taobao.com/outGetIpInfo";

    private static final Logger logger = LoggerFactory.getLogger(TaobaoAreaTask.class);
    private static final String DURATION = "common.task.duration.taobaoArea";
    @Resource
    private ProxyRepository proxyRepository;

    private static final int batchSize = 1000;
    private Integer maxPage = null;
    private Integer nowPage = 0;
    private RateLimiter limiter = RateLimiter.create(8D);

    public TaobaoAreaTask() {
        super(NumberUtils.toInt(SysConfig.getInstance().get(DURATION), 176400000));
    }

    private List<Proxy> find4Update() {
        if (maxPage == null) {
            // first run or reset page
            int totalRecord = proxyRepository.selectCount(new Proxy());
            nowPage = 0;
            maxPage = totalRecord / batchSize + 1;
        } else if (nowPage > maxPage) {// 这样的话,忽略本次查询,为了防止淘宝接口不稳定导致我们的调度任务卡死
            int totalRecord = proxyRepository.selectCount(new Proxy());
            nowPage = 0;
            maxPage = totalRecord / batchSize + 1;
            return Lists.newArrayList();
        }

        List<Proxy> areaUpdate = proxyRepository.find4AreaUpdate(PageRequest.of(nowPage, batchSize));
        nowPage++;
        return areaUpdate;
    }

    private Proxy getArea(String ipAddr) {
        if (limiter.tryAcquire(8, 1000, TimeUnit.SECONDS)) {
            Proxy proxy;
            JSONObject jsonObject;
            String response = null;
            try {
                Map<String, String> params = new HashMap<>();
                params.put("ip", ipAddr);
                params.put("accessKey", "alibaba-inc");
                response = HttpInvoker.post(TAOBAOURL, params);
                logger.info("request url:{}", TAOBAOURL + ipAddr);
                if (StringUtils.isEmpty(response)) {
                    return null;
                }
                jsonObject = JSONObject.parseObject(response);
                String data = jsonObject.get("data").toString();
                JSONObject temp = JSONObject.parseObject(data);
                proxy = JSON.parseObject(data, Proxy.class, Feature.IgnoreNotMatch);
                proxy.setCountryId(temp.get("country_id").toString());
                proxy.setAreaId(temp.get("area_id").toString());
                proxy.setRegionId(temp.get("region_id").toString());
                proxy.setCityId(temp.get("city_id").toString());
                proxy.setIspId(temp.get("isp_id").toString());
            } catch (Exception e) {
                logger.error("getAreaError:{}", response, e);

                proxy = new Proxy();
            }
            return proxy;
        } else {
            logger.info("QPS limit..." + ipAddr);
            return getArea(ipAddr);
        }
    }

    public void doSyncAddress(List<Proxy> proxyList) {
        for (Proxy proxy : proxyList) {
            try {
                Proxy area = getArea(proxy.getIp());
                if (area == null) {
                    continue;
                }
                area.setId(proxy.getId());
                if (StringUtils.isEmpty(area.getCountry()) && StringUtils.isEmpty(area.getArea())
                        && StringUtils.isEmpty(area.getIsp())) {
                    logger.warn("地址未知获取失败,response {},proxy:{}", JSONObject.toJSONString(area),
                            JSONObject.toJSONString(proxy));
                    continue;
                }
                proxyRepository.updateByPrimaryKeySelective(area);
            } catch (Exception e) {
                logger.error("同步地址信息失败 ", e);
            }
        }
    }

    @Override
    public Object execute() {
        logger.info("begin proxy address collect start");

        int maxTimes = 10;

        try {
            int i = 0;
            List<Proxy> proxyList = find4Update();
            while (proxyList.size() > 0) {
                i++;
                doSyncAddress(proxyList);
                proxyList = find4Update();
                if (i > maxTimes) {
                    return "";
                }
            }
        } catch (Exception e) {
            logger.error("error when address query", e);
        }
        return "";
    }
}
