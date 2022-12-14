package com.virjar.dungproxy.server.proxyservice.handler;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.proxyservice.client.NettyHttpClient;
import com.virjar.dungproxy.server.proxyservice.client.exception.ServerChannelInactiveException;
import com.virjar.dungproxy.server.proxyservice.client.exception.ServerChannelNotWritableException;
import com.virjar.dungproxy.server.proxyservice.client.listener.AbstractResponseListener;
import com.virjar.dungproxy.server.proxyservice.client.listener.DefaultRequestExecutor;
import com.virjar.dungproxy.server.proxyservice.client.listener.RequestExecutor;
import com.virjar.dungproxy.server.proxyservice.client.listener.RequestExecutorProxy;
import com.virjar.dungproxy.server.proxyservice.common.ProxyResponse;
import com.virjar.dungproxy.server.proxyservice.common.util.NetworkUtil;
import com.virjar.dungproxy.server.proxyservice.server.ProxySelector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.virjar.dungproxy.server.proxyservice.common.AttributeKeys.*;
import static com.virjar.dungproxy.server.proxyservice.common.Constants.CONNECTION_RESET_MSG;
import static com.virjar.dungproxy.server.proxyservice.common.ProxyResponse.TOO_MANY_CONNECTION_RESPONSE;
import static com.virjar.dungproxy.server.proxyservice.common.ProxyResponse.proxyError;
import static com.virjar.dungproxy.server.proxyservice.common.util.NetworkUtil.isCodeValid;

/**
 * Description: DrungProxyHandler
 *
 * @author lingtong.fu
 * @version 2016-10-31 04:27
 */
public class DrungProxyHandler extends EndpointHandler {

    private static final Logger log = LoggerFactory.getLogger(DrungProxyHandler.class);

    private int protocol;
    private String domain;
    private String clientIp;
    private String channelHex;

    private Proxy proxy;
    private Channel clientChannel;
    private FullHttpRequest request;
    private NettyHttpClient proxyClient;
    private AbstractResponseListener listener;
    private RequestExecutorProxy requestExecutor;
    private ProxySelector proxySelector;

    /**
     * Time
     */
    private long perRequestStart;
    private long totalRequestStart;
    private int requestTimeout;

    /**
     * ??????????????????
     */
    private int respCode;
    private int connectRetryCnt = 0;
    private int requestRetryCnt = 0;
    private static final int MAX_CONNECT_RETRY_CNT = 10;
    private static final int MAX_REQUEST_RETRY_CNT = 5;

    /**
     * ??????
     */
    private long traffic;
    private boolean hasWriteback = false;

    /**
     * ????????????
     */
    private String content;


    public DrungProxyHandler(Channel clientChannel, String clientIp) {
        this.clientChannel = clientChannel;
        this.clientIp = clientIp;
        this.channelHex = Integer.toHexString(this.clientChannel.hashCode());
        this.requestTimeout = NetworkUtil.getAttr(clientChannel, REQUEST_TIMEOUT);
        this.domain = NetworkUtil.getAttr(clientChannel, DOMAIN);
        this.proxySelector = NetworkUtil.getAttr(clientChannel, PROXY_SELECTOR_HOLDER);
        this.proxyClient = NetworkUtil.getAttr(clientChannel, SIMPLE_HTTP_CLIENT);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Preconditions.checkArgument(msg instanceof FullHttpRequest);
        Boolean customUserAgent = NetworkUtil.getAttr(ctx.channel(), CUSTOM_USER_AGENT);
        try {
            Optional<Proxy> proxyMeta = proxySelector.randomAvailableProxy(domain);
            if (proxyMeta.isPresent()) {
                proxy = proxyMeta.get();
            }
            request = (FullHttpRequest) msg;
            protocol = NetworkUtil.isSchemaHttps(request.uri()) ? 1 : 0;
            perRequestStart = totalRequestStart = System.currentTimeMillis();

            sendRequest(ctx, request.headers().contains(HttpHeaderNames.AUTHORIZATION), customUserAgent != null && customUserAgent, true);
        } catch (Exception e) {
            log.error("[FAILED] [] ????????????, ??????????????????, ??????:{} ??????: ", domain, e);
            ctx.channel().close();
        }
    }

    private void sendRequest(final ChannelHandlerContext ctx, final boolean customAuth, final boolean customUserAgent, final boolean retry) {
        //???????????????????????? request??????????????????
        if (request.refCnt() <= 0) {
            writeFailedResponse("Request Released");
            return;
        }
        perRequestStart = System.currentTimeMillis();
        listener = getAbstractResponseListener(ctx, customAuth, customUserAgent);
        log.info("START ????????????");
        if (request.refCnt() <= 0) {
            writeFailedResponse("Request Released");
            return;
        }
        if (!customUserAgent) {
            request.headers().set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36");
        }
        NettyHttpClient.RequestBuilder builder = getRequestBuilder(customAuth, retry, proxyClient);
        requestExecutor = builder.execute();
    }

    private NettyHttpClient.RequestBuilder getRequestBuilder(boolean customAuth, boolean retry, NettyHttpClient proxyClient) {
        NettyHttpClient.RequestBuilder builder = proxyClient.prepare(request, listener, proxy);

        int minus = (int) (System.currentTimeMillis() - totalRequestStart);
        if (minus < 0) {
            minus = 0;
        }
        int timeout = requestTimeout - minus;
        if (timeout < 0) {
            timeout = requestTimeout;
        }
        builder.setRequestTimeoutMs(timeout);
        builder.setExecutor(clientChannel.eventLoop());
        builder.setCustomAuth(customAuth);
        builder.setRetry(retry);
        return builder;
    }

    private AbstractResponseListener getAbstractResponseListener(final ChannelHandlerContext ctx, final boolean customAuth, final boolean customUserAgent) {
        return new AbstractResponseListener() {

            private String respStr;
            private int writeCnt;
            private int flushCnt;
            private long connectCompletedTime;
            private long handShakeSuccTime;
            private long headerReceivedTime;
            private long requestSentTime;
            private boolean useCachedConn;

            private String getTimeLine() {
                long sysTime = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                if (connectCompletedTime > 0) {
                    sb.append(useCachedConn);
                    sb.append(" connection time: ");
                    sb.append(connectCompletedTime - perRequestStart);
                    sb.append(" |");
                }
                if (handShakeSuccTime > 0) {
                    sb.append(" handshake time :");
                    sb.append(handShakeSuccTime - connectCompletedTime);
                    sb.append(" |");
                }
                if (requestSentTime > 0) {
                    sb.append(" request sent time:");
                    if (handShakeSuccTime > 0) {
                        sb.append(requestSentTime - handShakeSuccTime);
                    } else {
                        sb.append(requestSentTime - connectCompletedTime);
                    }
                    sb.append(" |");
                }
                if (headerReceivedTime > 0) {
                    sb.append(" header received time:");
                    sb.append(headerReceivedTime - requestSentTime);
                    sb.append(" data received time :");
                    sb.append(sysTime - headerReceivedTime);
                    sb.append(" |");
                }
                sb.append(" perRequest time :");
                sb.append(sysTime - perRequestStart);
                sb.append(" |");
                sb.append(" request total time:");
                sb.append(sysTime - totalRequestStart);
                return sb.toString();
            }

            @Override
            public State doOnConnectCompleted(Result result) {
                this.connectCompletedTime = System.currentTimeMillis();
                Object attr = result.getAttr();
                this.useCachedConn = (attr == null) ? false : (Boolean) attr;
                if (result.isSuccess()) {
                    return State.CONTINUE;
                } else {
                    log.info("[PROCESS] ???????????? [{}/{}] [{}] [{}] ms", result.getCause().getClass().getName(), result.getCause().getMessage(), connectRetryCnt, getTimeLine());
                    // ????????????????????????, ????????????????????????
                    if (++connectRetryCnt > MAX_CONNECT_RETRY_CNT) {
                        if (compareAndSetCompleted(false, true)) {
                            //????????????
                            respCode = -200;
                            log.info("[FAIL] ????????????????????????????????????, ?????????????????? {} ms", getTimeLine());
                            NetworkUtil.writeAndFlushAndClose(clientChannel, ProxyResponse.noAvailableProxy());
                        }
                    } else {
                        respCode = -200;
                        if (!isCompleted()) {
                            sendRequest(ctx, customAuth, true, true);
                        }
                    }
                    return State.ABORT;
                }
            }

            @Override
            public boolean doOnHeaderReceived(HttpResponse response) {
                respCode = response.status().code();
                this.respStr = response.status().reasonPhrase();
                this.headerReceivedTime = System.currentTimeMillis();

                HttpHeaders headers = response.headers();

                if (needRetry(respCode, headers)) {
                    if (isBlankPage(headers)) {  //?????????
                        respCode = -300;
                    }
                    boolean retry = requestRetryCnt++ < MAX_REQUEST_RETRY_CNT;
                    String msg = "[PROCESS] CODE [{}] ???????????? Header [{}] [{}] [{}] [{}] ms";
                    if (retry) msg += " ????????????";
                    log.info(msg, respCode, response.status().code(), response.status().reasonPhrase(), response.headers().entries(), getTimeLine());

                    if (retry) {
                        logRequestFailed(false);
                        requestExecutor.cancel();
                        if (!isCompleted()) {
                            sendRequest(ctx, customAuth, customUserAgent, requestRetryCnt < MAX_REQUEST_RETRY_CNT);
                        }
                    }
                    return retry;
                } else {
                    log.info(
                            "[PROCESS] ?????? Header [{}] [{}] [{}] [{}] ms",
                            response.status().code(),
                            response.status().reasonPhrase(),
                            response.headers().entries(),
                            getTimeLine()
                    );
                    return false;
                }
            }

            @Override
            public void doOnDataReceived(ByteBuf data) {
                NetworkUtil.releaseMsg(request);
                writeCnt++;
                int readableBytes = data.readableBytes();
                traffic += readableBytes;
                hasWriteback = true;

                if (!clientChannel.isActive()) {
                    NetworkUtil.releaseMsg(data);
                    clientChannel.flush();
                    if (compareAndSetCompleted(false, true)) {
                        log.info("[FAILED] ????????????, ??????????????????, [w/f {}/{}] [{}/{}] [{}] ms", writeCnt, flushCnt, respCode, respStr, getTimeLine());
                        executor.cancel(false);
                        //?????????????????????.
                        respCode = -500;
                        logRequestFailed(true);
                    }
                } else if (!clientChannel.isWritable()) {
                    NetworkUtil.releaseMsg(data);
                    clientChannel.flush();
                    if (compareAndSetCompleted(false, true)) {
                        log.info("[FAILED] ????????????, ??????????????????, [w/f {}/{}] [{}/{}] [{}] ms", writeCnt, flushCnt, respCode, respStr, getTimeLine());
                        executor.cancel(false);
                        //???????????????????????????, ?????????
                        respCode = -501;
                        logRequestFailed(true);
                    }
                } else {
                    clientChannel.write(data);
                }
            }

            @Override
            protected void doOnDataFlush(final ChannelHandlerContext serverContext, final boolean isLast) {
                NetworkUtil.releaseMsg(request);
                flushCnt++;
                clientChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            if (isLast) {
                                if (requestRetryCnt > 0) {
                                    log.info("The request through the retry is successful ! ");
                                }
                                if (isCodeValid(respCode)) {
                                    log.info("[SUCCESS] ???????????? [w/f {}/{}] ????????? [{}/{}] [Retry {}] [{}] ms", writeCnt, flushCnt, respCode, respStr, requestRetryCnt, getTimeLine());
                                    logRequestSuccess(DefaultRequestExecutor.shouldChannelBeKept(serverContext.channel()));
                                } else {
                                    log.info("[FAIL] ????????????, ????????? [{}/{}] [Retry {}] [{}] ms", respCode, respStr, requestRetryCnt, getTimeLine());
                                }
                            } else {
                                // ?????? read
                                if (!serverContext.channel().config().isAutoRead()) {
                                    serverContext.channel().read();
                                }
                            }
                        } else {
                            executor.cancel(false);
                            log.info("[FAILED] ????????????, ????????????????????? [{}] ms", getTimeLine(), future.cause());
                            // ?????????????????????
                            respCode = -502;
                            logRequestFailed(true);
                        }
                    }
                });
            }

            @Override
            public void doOnRequestTimeout() {
                respCode = -400;
                NetworkUtil.releaseMsg(request);
                log.info("[FAIL] ?????? [{}/{}] ms", getTimeLine(), System.currentTimeMillis() - requestSentTime);
                logRequestFailed(true);
                NetworkUtil.writeAndFlushAndClose(clientChannel, ProxyResponse.proxyTimeout(proxy.getId()));
            }

            @Override
            public void doOnConnectionPoolIsFull() {
                NetworkUtil.releaseMsg(request);
                NetworkUtil.writeAndFlushAndClose(clientChannel, TOO_MANY_CONNECTION_RESPONSE);
            }

            @Override
            public void doOnThrowable(String errorTrace, Throwable cause) {
                clientChannel.flush();
                respCode = getExceptionCode(cause);
                if (respCode == -1000) {
                    log.error("[FAIL] ???????????? Trace {} [{}] ms", errorTrace, getTimeLine(), cause);
                } else {
                    log.error("[FAIL] ???????????? Trace {} [{}] ms [{}]", errorTrace, getTimeLine(), cause.getMessage());
                }
                if ((requestRetryCnt++ < MAX_REQUEST_RETRY_CNT) && !hasWriteback) {
                    log.warn("[PROCESS] ???????????? []");
                    logRequestFailed(false);
                    sendRequest(ctx, customAuth, true, requestRetryCnt < MAX_REQUEST_RETRY_CNT);
                } else {
                    logRequestFailed(true);
                    NetworkUtil.releaseMsg(request);
                    writeFailedResponse("Unknown ex " + cause.getClass().getSimpleName());
                }
            }

            @Override
            protected void doOnHandshakeSuccess() {
                this.handShakeSuccTime = System.currentTimeMillis();
            }

            @Override
            protected void doOnRequestSent() {
                this.requestSentTime = System.currentTimeMillis();
            }

            @Override
            public void setRequestExecutor(RequestExecutor executor) {
                this.executor = executor;
            }

            @Override
            public boolean compareAndSetCompleted(boolean expect, boolean update) {
                NetworkUtil.releaseMsgCompletely(request);
                NetworkUtil.removeHandler(clientChannel.pipeline(), DrungProxyHandler.class);
                return super.compareAndSetCompleted(expect, update);
            }
        };
    }

    private static boolean needRetry(int respCode, HttpHeaders headers) {
        return ((respCode == 200 && isBlankPage(headers)));
    }

    private static boolean isBlankPage(HttpHeaders headers) {
        String ret = headers.get(HttpHeaderNames.CONTENT_LENGTH);
        return ret != null && ret.equals("0");
    }

    private void logRequestFailed(boolean finished) {
        long now = System.currentTimeMillis();
        log.info("??????????????????, ??????:{} ms", now - totalRequestStart);
        if (finished) clientChannel.close();
    }

    private void logRequestSuccess(boolean keepAlive) {
        long now = System.currentTimeMillis();
        log.info("????????????, ??????:{} ms", now - totalRequestStart);
        if (!keepAlive) {
            clientChannel.close();
        }
    }

    private int getExceptionCode(Throwable cause) {
        if (cause == null) {
            //????????????
            return -1000;
        }
        if (cause instanceof ServerChannelInactiveException) {
            //????????????????????????.
            return -600;
        }
        if (cause instanceof ServerChannelNotWritableException) {
            //???????????????????????????, ?????????
            return -601;
        }
        String msg = cause.getMessage();
        if (msg == null) {
            return -1000;
        }
        //????????????
        return cause.getMessage().equals(CONNECTION_RESET_MSG) ? -1001 : -1000;
    }

    private void writeFailedResponse(String message) {
        log.error("The Error msg is: {} channel No:{}", message, channelHex);
        NetworkUtil.writeAndFlushAndClose(clientChannel, proxyError(proxy != null ? proxy.getId() : 0, message, channelHex));
    }

}
