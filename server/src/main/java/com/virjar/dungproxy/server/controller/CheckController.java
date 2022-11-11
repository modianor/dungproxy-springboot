package com.virjar.dungproxy.server.controller;

import com.virjar.dungproxy.server.core.beanmapper.BeanMapper;
import com.virjar.dungproxy.server.core.rest.ResponseEnvelope;
import com.virjar.dungproxy.server.core.utils.ReturnUtil;
import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.model.AvailbelCheckResponse;
import com.virjar.dungproxy.server.model.ProxyModel;
import com.virjar.dungproxy.server.scheduler.DomainTestTask;
import com.virjar.dungproxy.server.scheduler.NonePortResourceTester;
import com.virjar.dungproxy.server.service.ProxyService;
import com.virjar.dungproxy.server.utils.Constant;
import com.virjar.dungproxy.server.utils.ProxyUtil;
import com.virjar.dungproxy.server.utils.ResourceFilter;
import com.virjar.dungproxy.server.utils.Tranparent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by virjar on 16/8/14.
 */
@Controller
@RequestMapping("/proxyipcenter")
public class CheckController {

    @Resource
    private ProxyService proxyService;

    @Resource
    private BeanMapper beanMapper;

    @RequestMapping(value = "/portCheck", method = RequestMethod.GET)
    public ResponseEntity<ResponseEnvelope<Object>> checkPort(@RequestParam("ip") String ip) {
        boolean b = NonePortResourceTester.sendIp(ip);
        return ReturnUtil.retSuccess(b);
    }

    @RequestMapping(value = "/checkUrlTask", method = RequestMethod.GET)
    public ResponseEntity<ResponseEnvelope<Object>> checkUrlTask(@RequestParam("url") String url) {
        Boolean b = DomainTestTask.sendDomainTask(url);
        return ReturnUtil.retSuccess(b);
    }

    @RequestMapping(value = "/checkIp", method = RequestMethod.GET) // , produces = MediaType.ALL_VALUE 这个玩意儿有大问题
    public ResponseEntity<ResponseEnvelope<Object>> getDomainqueueById(HttpServletRequest request) {
        byte transparent = checkTransparent(request);
        String remoteAddr = request.getRemoteAddr();
        AvailbelCheckResponse availbelCheckResponse = new AvailbelCheckResponse();
        availbelCheckResponse.setTransparent(transparent);
        availbelCheckResponse.setKey(AvailbelCheckResponse.staticKey);
        availbelCheckResponse.setRemoteAddr(remoteAddr);
        String header = request.getHeader(Constant.HEADER_CHECK_HEADER);
        availbelCheckResponse.setLostHeader(StringUtils.isEmpty(header));

        String ip = request.getParameter("ip");
        String port = request.getParameter("port");
        if (!StringUtils.isEmpty(ip) && !StringUtils.isEmpty(port)) {
            Proxy proxy = new Proxy();
            proxy.setIp(ip);
            proxy.setPort(NumberUtils.toInt(port, 3128));
            proxy.setIpValue(ProxyUtil.toIPValue(ip));
            if (!ResourceFilter.contains(proxy)) {
                proxyService.createSelective(beanMapper.map(proxy, ProxyModel.class));
            }
        }
        return ReturnUtil.retSuccess(availbelCheckResponse);
    }

    private byte checkTransparent(HttpServletRequest request) {

        String ipAddress = request.getHeader("x-forwarded-for");
        if (!StringUtils.isEmpty(ipAddress)) {
            if (StringUtils.equalsIgnoreCase("unknown", ipAddress)) {
                return Tranparent.anonymous.getValue();
            } else {
                return Tranparent.transparent.getValue();
            }
        }
        ipAddress = request.getHeader("Proxy-Client-IP");
        if (!StringUtils.isEmpty(ipAddress)) {
            if (StringUtils.equalsIgnoreCase("unknown", ipAddress)) {
                return Tranparent.anonymous.getValue();
            } else {
                return Tranparent.transparent.getValue();
            }
        }
        ipAddress = request.getHeader("http_vir");
        if (!StringUtils.isEmpty(ipAddress)) {
            if (StringUtils.equalsIgnoreCase("unknown", ipAddress)) {
                return Tranparent.anonymous.getValue();
            } else {
                return Tranparent.transparent.getValue();
            }
        }
        return Tranparent.highAnonymous.getValue();
    }
}
