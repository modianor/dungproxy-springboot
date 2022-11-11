package com.virjar.dungproxy.client.ippool.config;

/**
 * Created by virjar on 16/9/30. 全局配置项
 */
public class ProxyConstant {
    public static final String USED_PROXY_KEY = "USED_PROXY_KEY";
    /**
     * 在httpclient里面,如果存在这个key值为true,则取消代理
     */
    public static final String DISABLE_DUNGPROXY_KEY = "DISABLE_DUNGPROXY_KEY";

    /**
     * 用户的key值,在httpclientContext里面,同这个key值确定是那个用户在访问,以实现用户隔离
     */
    public static final String DUNGPROXY_USER_KEY = "DUNGPROXY_USER_KEY";

    // config 文件默认配置key值
    static final String RESOURCE_FACADE = "proxyclient.resouce.resourceFacade";
    static final String PROXY_DOMAIN_STRATEGY = "proxyclient.proxyDomainStrategy";
    static final String DEFAULT_DOMAIN_STRATEGY = "WHITE_LIST";
    static final String WHITE_LIST_STRATEGY = "proxyclient.proxyDomainStrategy.whiteList";
    static final String BLACK_LIST_STRATEGY = "proxyclient.proxyDomainStrategy.blackList";
    static final String FEEDBACK_DURATION = "proxyclient.feedback.duration";
    static final String DEFAULT_RESOURCE_SERVER_ADDRESS = "proxyclient.resource.defaultResourceServerAddress";

    static final String PROXY_USE_INTERVAL = "proxyclient.proxyUseIntervalMillis";
    static final String CLIENT_ID = "proxyclient.clientID";
    static final String PROXY_DOMAIN_STRATEGY_ROUTE = "proxyclient.proxyDomainStrategy.group";

    static final String PREHEATER_TASK_LIST = "proxyclient.preHeater.testList";
    static final String PREHEAT_SERIALIZE_STEP = "proxyclient.preHeater.serialize.step";
    public static String CLIENT_CONFIG_FILE_NAME = "proxyclient.properties";
    static final String PROXY_SERIALIZER = "proxyclient.serialize.serializer";
    static final String DEFAULT_PROXY_SERALIZER_FILE = "proxyclient.DefaultAvProxyDumper.dumpFileName";

    public static final String DEFAULT_PROXY_SERALIZER_FILE_VALUE = "availableProxy.json";

    // 以下几个超时时间允许被调整
    // socket超时时间
    public static int SOCKET_TIMEOUT = 10000;
    // 连接超时
    public static int CONNECT_TIMEOUT = 10000;
    // 连接池分配连接超时时间,一般用处不大
    public static int REQUEST_TIMEOUT = 30000;
    public static int SOCKETSO_TIMEOUT = 15000;
}
