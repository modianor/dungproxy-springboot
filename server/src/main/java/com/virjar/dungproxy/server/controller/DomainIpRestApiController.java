package com.virjar.dungproxy.server.controller;

import com.virjar.dungproxy.server.core.beanmapper.BeanMapper;
import com.virjar.dungproxy.server.core.rest.ResponseEnvelope;
import com.virjar.dungproxy.server.core.utils.ReturnCode;
import com.virjar.dungproxy.server.core.utils.ReturnUtil;
import com.virjar.dungproxy.server.model.DomainIpModel;
import com.virjar.dungproxy.server.service.DomainIpService;
import com.virjar.dungproxy.server.vo.DomainIpVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/proxyipcenter")
public class DomainIpRestApiController {
    private final Logger logger = LoggerFactory.getLogger(DomainIpRestApiController.class);

    @Resource
    private BeanMapper beanMapper;

    @Resource
    private DomainIpService domainIpService;

    @RequestMapping(value = "/domainip/{id}", method = RequestMethod.GET)
    public ResponseEntity<ResponseEnvelope<Object>> getDomainIpById(@PathVariable Long id) {
        DomainIpModel domainIpModel = domainIpService.findByPrimaryKey(id);
        DomainIpVO domainIpVO = beanMapper.map(domainIpModel, DomainIpVO.class);
        return ReturnUtil.retSuccess(domainIpVO);
    }

    @RequestMapping(value = "/domainip", method = RequestMethod.POST)
    public ResponseEntity<ResponseEnvelope<Object>> createDomainIp(@RequestBody @Valid DomainIpVO domainIpVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ReturnUtil.retException(ReturnCode.INPUT_PARAM_ERROR, errorMessage);
        }
        DomainIpModel domainIpModel = beanMapper.map(domainIpVO, DomainIpModel.class);
        Integer id = domainIpService.create(domainIpModel);
        return ReturnUtil.retSuccess(id);
    }

    @RequestMapping(value = "/domainip/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseEnvelope<Object>> deleteDomainIpByPrimaryKey(@PathVariable Long id) {
        Integer result = domainIpService.deleteByPrimaryKey(id);
        if (result == 1) {
            return ReturnUtil.retSuccess(result);
        } else {
            return ReturnUtil.retException(ReturnCode.RECORD_NOT_EXIST, "id=" + id);
        }
    }

    @RequestMapping(value = "/domainip/{id}", method = RequestMethod.PUT)
    public ResponseEntity<ResponseEnvelope<Object>> updateDomainIpByPrimaryKeySelective(@PathVariable Long id,
            @RequestBody @Valid DomainIpVO domainIpVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ReturnUtil.retException(ReturnCode.INPUT_PARAM_ERROR, errorMessage);
        }
        DomainIpModel domainIpModel = beanMapper.map(domainIpVO, DomainIpModel.class);
        domainIpModel.setId(id);
        Integer result = domainIpService.updateByPrimaryKeySelective(domainIpModel);
        if (result == 1) {
            return ReturnUtil.retSuccess(id);
        } else {
            return ReturnUtil.retException(ReturnCode.RECORD_NOT_EXIST, "id=" + id);
        }
    }

    @RequestMapping(value = "/domainip/list")
    public ResponseEntity<ResponseEnvelope<Object>> listDomainIps(@PageableDefault Pageable pageable,
            DomainIpVO domainIpVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ReturnUtil.retException(ReturnCode.INPUT_PARAM_ERROR, errorMessage);
        }
        List<DomainIpModel> domainIpModels = domainIpService.selectPage(
                beanMapper.map(domainIpVO, DomainIpModel.class), pageable);
        Page<DomainIpVO> page = new PageImpl<DomainIpVO>(beanMapper.mapAsList(domainIpModels, DomainIpVO.class),
                pageable, domainIpService.selectCount(beanMapper.map(domainIpVO, DomainIpModel.class)));
        return ReturnUtil.retSuccess(page);
    }
}