package com.virjar.dungproxy.server.controller;

import com.google.common.collect.Maps;
import com.virjar.dungproxy.server.core.rest.ResponseEnvelope;
import com.virjar.dungproxy.server.core.utils.ReturnUtil;
import com.virjar.dungproxy.server.entity.Source;
import com.virjar.dungproxy.server.service.SourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proxyipcenter")
public class SourceRestApiController {
    private final Logger logger = LoggerFactory.getLogger(SourceRestApiController.class);
    @Resource
    private SourceService sourceService;

    @RequestMapping("/source")
    public ResponseEntity<ResponseEnvelope<Object>> source() {
        Map<String, Object> ret = Maps.newHashMap();
        List<Source> sources = sourceService.selectAllSource();
        ret.put("num", sources.size());
        ret.put("source", sources);
        ResponseEntity<ResponseEnvelope<Object>> responseEnvelopeResponseEntity = ReturnUtil.retSuccess(ret);
        return responseEnvelopeResponseEntity;
    }
}