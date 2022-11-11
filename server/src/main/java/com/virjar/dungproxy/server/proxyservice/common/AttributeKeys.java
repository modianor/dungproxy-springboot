package com.virjar.dungproxy.server.proxyservice.common;

import com.virjar.dungproxy.server.proxyservice.client.NettyHttpClient;
import com.virjar.dungproxy.server.proxyservice.server.ProxySelector;
import io.netty.util.AttributeKey;

import static io.netty.util.AttributeKey.valueOf;

/**
 * Description: AttributeKeys
 *
 * @author lingtong.fu
 * @version 2016-11-07 16:07
 */
public class AttributeKeys {

    public static final AttributeKey<Integer> REQUEST_TIMEOUT = valueOf("requestTimeout");

    public static final AttributeKey<String> DOMAIN = valueOf("domain");

    public static final AttributeKey<ProxySelector> PROXY_SELECTOR_HOLDER = valueOf("proxySelectorHolder");

    public static final AttributeKey<NettyHttpClient> SIMPLE_HTTP_CLIENT = valueOf("simpleHttpClient");

    public static final AttributeKey<Boolean> CUSTOM_USER_AGENT = valueOf("cusUserAgent");

    public final static AttributeKey<Boolean> KEEP_ALIVE = AttributeKey.valueOf("keep-alive");

}
