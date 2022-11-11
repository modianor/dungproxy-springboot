package com.virjar.dungproxy.server.proxyservice.handler.checker;

import com.google.common.base.Strings;
import com.virjar.dungproxy.client.util.CommonUtil;
import com.virjar.dungproxy.server.proxyservice.common.AttributeKeys;
import com.virjar.dungproxy.server.proxyservice.common.util.NetworkUtil;
import com.virjar.dungproxy.server.proxyservice.handler.ClientProcessHandler;
import com.virjar.dungproxy.server.proxyservice.handler.DrungProxyHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.virjar.dungproxy.server.proxyservice.common.AttributeKeys.REQUEST_TIMEOUT;
import static com.virjar.dungproxy.server.proxyservice.common.Constants.*;
import static io.netty.util.AttributeKey.valueOf;

/**
 * Description: RequestChecker
 *
 * @author lingtong.fu
 * @version 2016-10-31 02:56
 */
@ChannelHandler.Sharable
public class RequestValidator extends ClientProcessHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    private static final int DEFAULT_REQUEST_TIMEOUT = 60000;

    private static final int MAX_REQUEST_TIMEOUT = 10 * DEFAULT_REQUEST_TIMEOUT;

    public static final RequestValidator instance = new RequestValidator();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
            HttpRequest request = (HttpRequest) msg;
            HttpVersion version = request.protocolVersion();
            HttpMethod method = request.method();

            log.info("收到请求 [CH] [{}] Request [{}] [{}] [{}]", ctx.channel(), version, method, NetworkUtil.requestToCurl(request));

            setIfHttps(request);
            checkHttpMethod(request);
            checkExpected100Continue(request);
            setHttpVersion(request);

            // 用户是否自定义userAgent
            checkUserAgent(request, ctx);
            clearProxyHeaders(request);
            // 用户是否自定义超时时间
            handleTimeout(ctx, request);
            setHostFromRequest(ctx, request);
            // 代理请求处理
            NetworkUtil.resetHandler(ctx.pipeline(), new DrungProxyHandler(ctx.channel(), NetworkUtil.getIp(ctx.channel())));

            super.channelRead(ctx, msg);
        } catch (Exception e) {
            NetworkUtil.releaseMsgCompletely(msg);
            throw e;
        }
    }

    private void setHostFromRequest(ChannelHandlerContext ctx, HttpRequest request) {
        String host = CommonUtil.extractDomain(request.headers().get(HttpHeaderNames.HOST));
        NetworkUtil.setAttr(ctx.channel(), AttributeKeys.DOMAIN, host);
    }

    private void setIfHttps(HttpRequest request) {
        String uri = request.uri();
        String useHttps = request.headers().get(USE_HTTPS_KEY);
        if (useHttps != null && useHttps.equals("1")) {
            if (uri.substring(0, 5).toLowerCase().equals("http:")) {
                uri = uri.substring(0, 4) + "s" + uri.substring(4);
            }
        }
        request.setUri(uri);
    }

    private void checkHttpMethod(HttpRequest request) {
        if (request.method().equals(HttpMethod.GET) && request.headers().get(HttpHeaderNames.CONTENT_LENGTH) != null) {
            request.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
        }
    }

    //支持目标网站的HTTP的版本号为1.0或之前的版本
    private void checkExpected100Continue(HttpRequest request) {
        if (HttpUtil.is100ContinueExpected(request)) {
            request.headers().remove(HttpHeaderNames.EXPECT);
        }
    }

    private void setHttpVersion(HttpRequest request) {
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
    }

    private void checkUserAgent(HttpRequest request, ChannelHandlerContext ctx) {
        String userAgent = request.headers().get(CUSTOM_USER_AGENT_KEY);
        if (userAgent != null) {
            NetworkUtil.setAttr(ctx.channel(), valueOf("cusUserAgent"), true);
        } else {
            NetworkUtil.setAttr(ctx.channel(), valueOf("cusUserAgent"), false);
        }
    }

    private void clearProxyHeaders(HttpRequest request) {
        for (String header : PROXY_HEADER_SET) {
            request.headers().remove(header);
        }
    }

    private void handleTimeout(ChannelHandlerContext ctx, HttpRequest request) {
        String str = request.headers().get(REQ_TTL_KEY);
        int timeout = DEFAULT_REQUEST_TIMEOUT;
        if (!Strings.isNullOrEmpty(str)) {
            try {
                timeout = Integer.valueOf(str);
                if (timeout < 0 || timeout > MAX_REQUEST_TIMEOUT) {
                    timeout = DEFAULT_REQUEST_TIMEOUT;
                }
            } catch (NumberFormatException e) {
                timeout = DEFAULT_REQUEST_TIMEOUT;
            }
        }
        NetworkUtil.setAttr(ctx.channel(), REQUEST_TIMEOUT, timeout);
    }
}
