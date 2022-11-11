package com.virjar.dungproxy.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.server.model.AvailbelCheckResponse;
import com.virjar.dungproxy.server.model.ProxyModel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * ClassName:ProxyUtil
 *
 * @author ch
 * @version Ver 1.0
 * @Date 2014-2-16 下午04:20:07
 * @see
 */
public class ProxyUtil {
    private static final String keysourceurl = SysConfig.getInstance().getKeyverifyurl();
    private static InetAddress localAddr;
    private static final Logger logger = LoggerFactory.getLogger(ProxyUtil.class);

    static {
        init();
    }

    private static void init() {
        Enumeration<InetAddress> localAddrs;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                localAddrs = ni.getInetAddresses();
                while (localAddrs.hasMoreElements()) {
                    InetAddress tmp = localAddrs.nextElement();
                    if (!tmp.isLoopbackAddress() && !tmp.isLinkLocalAddress() && !(tmp instanceof Inet6Address)) {
                        localAddr = tmp;
                        logger.info("local IP:" + localAddr.getHostAddress());
                        return;
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failure when init ProxyUtil", e);
            logger.error("choose NetworkInterface\n" + getNetworkInterface());
        }

    }

    /**
     * socket是否区分V4 & V5? 看javaAPI好像可以自动识别,那么其他API是否也应该能自动识别了?
     *
     * @param p
     * @return
     */
    private static AvailbelCheckResponse socketCheck(ProxyModel p) {
        InputStream is = null;
        try {
            long start = System.currentTimeMillis();
            SocketAddress socketAddress = new InetSocketAddress(p.getIp(), p.getPort());
            URL url = new URL(keysourceurl);
            URLConnection urlConnection = url.openConnection(new Proxy(Proxy.Type.SOCKS, socketAddress));
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(1000);
            urlConnection.setReadTimeout(3000);
            is = urlConnection.getInputStream();
            String response = IOUtils.toString(is);
            AvailbelCheckResponse availbelCheckResponse = JSONUtils.parse(response,
                    AvailbelCheckResponse.class);
            if (availbelCheckResponse != null
                    && AvailbelCheckResponse.staticKey.equals(availbelCheckResponse.getKey())) {
                availbelCheckResponse.setSpeed(System.currentTimeMillis() - start);
                availbelCheckResponse.setType(ProxyType.SOCKET.getType());
                return availbelCheckResponse;
            }
        } catch (Exception e) {
            // doNothing
            // e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return null;
    }

    public static void main(String[] args) throws UnknownHostException {
        // ProxyModel proxyModel = new ProxyModel();
        // proxyModel.setIp("202.106.16.36");
        // proxyModel.setPort(3128);
        // AvailbelCheckResponse availbelCheckResponse = httpCheck(proxyModel);
        // System.out.println(JSONObject.toJSONString(availbelCheckResponse));
        // HttpHost httpHost =new HttpHost(InetAddress.getByName("187.161.105.133"), 53796);

        // validateProxyConnect(httpHost);
        ProxyModel proxyModel = new ProxyModel();
        proxyModel.setIp("202.106.16.36");
        proxyModel.setPort(3128);
        AvailbelCheckResponse availbelCheckResponse = validateProxyAvailable(proxyModel);
        System.out.println(JSONObject.toJSONString(availbelCheckResponse));
    }

    private static AvailbelCheckResponse httpCheck(ProxyModel p) {
        try {
            long start = System.currentTimeMillis();
            String response = HttpInvoker.get(keysourceurl + "?ip=" + p.getIp() + "&port=" + p.getPort(),
                    new Header[]{Constant.CHECK_HEADER}, p.getIp(), p.getPort());
            if (StringUtils.isEmpty(response)) {
                return null;
            }

            if (!response.contains("百度")) {
                return null;
            }
            /*AvailbelCheckResponse availbelCheckResponse = JSONUtils.parse(response, AvailbelCheckResponse.class);*/
            AvailbelCheckResponse availbelCheckResponse = new AvailbelCheckResponse();
            availbelCheckResponse.setKey(AvailbelCheckResponse.staticKey);
            if (availbelCheckResponse != null
                    && AvailbelCheckResponse.staticKey.equals(availbelCheckResponse.getKey())) {
                availbelCheckResponse.setSpeed(System.currentTimeMillis() - start);
                availbelCheckResponse.setType(ProxyType.HTTP.getType());
                return availbelCheckResponse;
            } else if (StringUtils.isNotBlank(response) && SysConfig.getInstance().recordFaildResponse()) {
                logger.info("checker error response is {}", response);
            }
        } catch (Exception e) {
            // doNothing
        }
        return null;
    }

    public static boolean checkUrl(ProxyModel proxy, String url) {
        return checkUrl(proxy.getIp(), proxy.getPort(), url);
    }

    public static boolean checkUrl(String ip, int port, String url) {
        for (int i = 0; i < 3; i++) {
            // 可能有问题,资源不可用,响应也有可能不是空。需要客户端代理失败决策方案出来之后进行优化
            int status = HttpInvoker.getStatus(url, ip, port);
            if (status == -1) {
                continue;
            }
            return status == 200;
        }
        return false;
    }

    /**
     * 可用性验证在本方法计算响应时间
     *
     * @param p 代理
     * @return
     */
    public static AvailbelCheckResponse validateProxyAvailable(ProxyModel p) {
        if (p.getType() == null) {
            AvailbelCheckResponse availbelCheckResponse = httpCheck(p);
            if (availbelCheckResponse != null) {
                return availbelCheckResponse;
            }
            availbelCheckResponse = socketCheck(p);
            if (availbelCheckResponse != null) {
                return availbelCheckResponse;
            }
            return null;
        } else {
            ProxyType type = ProxyType.from(p.getType());
            if (type == null) {
                logger.error("不能识别的已定义代理类型:{},代理为:{}", p.getType(), JSON.toJSONString(p));
                return null;
            }
            switch (type) {
                case HTTP:
                case HTTPHTTPS:
                    return httpCheck(p);
                case SOCKET:
                    return socketCheck(p);
                default:
                    return null;// 不会发生
            }
        }
    }

    private static Socket newLocalSocket() {
        for (int i = 0; i < 3; i++) {
            Socket socket = new Socket();
            try {
                socket.bind(new InetSocketAddress(localAddr, 0));
                return socket;
            } catch (IOException e) {
                logger.warn("系统资源不足,本地端口开启失败");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Boolean validateProxyConnect(HttpHost p) {
        if (localAddr == null) {
            logger.error("cannot get local ip");
            throw new IllegalStateException("cannot get local ip");
        }
        Socket socket = newLocalSocket();
        if (socket == null) {
            return null;
        }
        try {
            InetSocketAddress endpointSocketAddr = new InetSocketAddress(p.getAddress().getHostAddress(), p.getPort());
            socket.connect(endpointSocketAddr, 4000);
            return true;
        } catch (Exception e) {
            // 日志级别为debug,失败的IP数量非常多
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return false;
    }

    private static String getNetworkInterface() {
        String networkInterfaceName = "";
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        while (enumeration.hasMoreElements()) {
            NetworkInterface networkInterface = enumeration.nextElement();
            networkInterfaceName += networkInterface.toString() + '\n';
            Enumeration<InetAddress> addr = networkInterface.getInetAddresses();
            while (addr.hasMoreElements()) {
                networkInterfaceName += "\tip:" + addr.nextElement().getHostAddress() + "\n";
            }
        }
        return networkInterfaceName;
    }

    public static Long toIPValue(String ipAddress) {
        List<String> strings = Splitter.on(".").trimResults().splitToList(ipAddress);
        Long ret = 0L;
        ret |= Long.parseLong(strings.get(0)) << 24;
        ret |= Long.parseLong(strings.get(1)) << 16;
        ret |= Long.parseLong(strings.get(2)) << 8;
        ret |= Long.parseLong(strings.get(3));
        return ret;
    }
}
