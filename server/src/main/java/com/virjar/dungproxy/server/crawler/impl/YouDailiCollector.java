package com.virjar.dungproxy.server.crawler.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.virjar.dungproxy.server.crawler.NewCollector;
import com.virjar.dungproxy.server.entity.Proxy;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by virjar on 16/11/26.
 */
@Component
public class YouDailiCollector extends NewCollector {
    private static final Logger logger = LoggerFactory.getLogger(YouDailiCollector.class);
    private static Pattern ipAndPortPattern = Pattern.compile(
            "(([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}):(\\d+)");
    private String seed = "http://www.youdaili.net/Daili/guonei/";
    private String lasUrl = seed;// 仅仅为了统计

    //    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
    public YouDailiCollector() {
        setDuration(1);
    }

    @Override
    public String lasUrl() {
        return lasUrl;
    }

    @Override
    public List<Proxy> doCollect() {
        List<Proxy> ret = Lists.newArrayList();
        Jedis jedis = new Jedis("10.120.80.58", 6379);
        jedis.auth("miocrawler20200228");
        jedis.select(1);
        Set<String> proxies = jedis.zrange("proxies", 0, 20);
        for (String proxyStr : proxies) {
            String[] ipPort = proxyStr.split(":");
            String ip = ipPort[0];
            String portStr = ipPort[1];
            Integer port = NumberUtils.toInt(portStr);
            logger.info("Redis获取到Proxy：{}", proxyStr);
            Proxy proxy = new Proxy();
            proxy.setIp(ip);
            proxy.setPort(port);
            proxy.setConnectionScore(0L);
            proxy.setAvailbelScore(0L);
            proxy.setSource(lasUrl());
            proxy.setCreatetime(new Date());
            ret.add(proxy);
        }
        // 3. 关闭连接
        jedis.close();
        return ret;
    }


    public static void main(String[] args) {
        List<Proxy> proxies = new YouDailiCollector().doCollect();
        System.out.println(JSONObject.toJSONString(proxies));
    }
}
