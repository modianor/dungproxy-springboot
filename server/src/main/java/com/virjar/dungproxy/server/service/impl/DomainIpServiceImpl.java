package com.virjar.dungproxy.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.virjar.dungproxy.server.core.beanmapper.BeanMapper;
import com.virjar.dungproxy.server.entity.DomainIp;
import com.virjar.dungproxy.server.model.DomainIpModel;
import com.virjar.dungproxy.server.model.ProxyModel;
import com.virjar.dungproxy.server.repository.DomainIpRepository;
import com.virjar.dungproxy.server.repository.ProxyRepository;
import com.virjar.dungproxy.server.service.DomainIpService;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class DomainIpServiceImpl implements DomainIpService {
    private static final Logger logger = LoggerFactory.getLogger(DomainIpServiceImpl.class);
    @Resource
    private BeanMapper beanMapper;

    @Resource
    private DomainIpRepository domainIpRepo;

    @Resource
    private ProxyRepository proxyRepository;

    @Transactional
    @Override
    public int create(DomainIpModel domainIpModel) {

        DomainIp domainIp = beanMapper.map(domainIpModel, DomainIp.class);
        DomainIp query = new DomainIp();
        // 逻辑上,这三个字段作为主键
        query.setIp(domainIp.getIp());
        query.setPort(domainIp.getPort());
        query.setDomain(domainIp.getDomain());
        List<DomainIp> domainIps = domainIpRepo.selectPage(query, PageRequest.of(0, 1));
        if (domainIps.size() > 0) {
            // update
            DomainIp originDomainIp = domainIps.get(0);
            domainIp.setId(originDomainIp.getId());
            if (originDomainIp.getDomainScore() <= 0) {
                domainIp.setDomainScore(1L);
            } else {
                domainIp.setDomainScore(originDomainIp.getDomainScore() + 1);
            }
            domainIp.setDomainScoreDate(new Date());
            domainIp.setTestUrl(domainIpModel.getTestUrl());
            return domainIpRepo.updateByPrimaryKeySelective(domainIp);
        } else {
            // insert
            if (domainIp.getDomainScore() == null) {
                domainIp.setDomainScore(1L);
            }
            domainIp.setCreatetime(new Date());
            return domainIpRepo.insert(domainIp);
        }
    }

    @Transactional
    @Override
    public int createSelective(DomainIpModel domainIpModel) {
        return domainIpRepo.insertSelective(beanMapper.map(domainIpModel, DomainIp.class));
    }

    @Transactional
    @Override
    public int deleteByPrimaryKey(Long id) {
        return domainIpRepo.deleteByPrimaryKey(id);
    }

    @Transactional(readOnly = true)
    @Override
    public DomainIpModel findByPrimaryKey(Long id) {
        DomainIp domainIp = domainIpRepo.selectByPrimaryKey(id);
        return beanMapper.map(domainIp, DomainIpModel.class);
    }

    @Transactional(readOnly = true)
    @Override
    public int selectCount(DomainIpModel domainIpModel) {
        return domainIpRepo.selectCount(beanMapper.map(domainIpModel, DomainIp.class));
    }

    @Transactional
    @Override
    public int updateByPrimaryKey(DomainIpModel domainIpModel) {
        return domainIpRepo.updateByPrimaryKey(beanMapper.map(domainIpModel, DomainIp.class));
    }

    @Transactional
    @Override
    public int updateByPrimaryKeySelective(DomainIpModel domainIpModel) {
        return domainIpRepo.updateByPrimaryKeySelective(beanMapper.map(domainIpModel, DomainIp.class));
    }

    @Transactional(readOnly = true)
    @Override
    public List<DomainIpModel> selectPage(DomainIpModel domainIpModel, Pageable pageable) {
        List<DomainIp> domainIpList = domainIpRepo.selectPage(beanMapper.map(domainIpModel, DomainIp.class), pageable);
        return beanMapper.mapAsList(domainIpList, DomainIpModel.class);
    }

    @Override
    public List<ProxyModel> convert(List<DomainIpModel> domainIpModels) {
        // List<ProxyModel> ret = Lists.newArrayList();
        // 不能这么做,这样会有大量查询请求,为处理瓶颈
        List<Long> ids = Lists.newArrayList();
        if (domainIpModels.size() == 0) {
            return Lists.newArrayList();
        }
        for (DomainIpModel domainIpModel : domainIpModels) {
            ids.add(domainIpModel.getProxyId());
        }
        return beanMapper.mapAsList(proxyRepository.selectByIds(ids), ProxyModel.class);
        /*
         * for (DomainIpModel domainIpModel : domainIpModels) { Proxy proxy =
         * proxyRepository.selectByPrimaryKey(domainIpModel.getProxyId()); if (proxy != null) {
         * ret.add(beanMapper.map(proxy, ProxyModel.class)); } }
         */
        // return ret;
    }

    @Override
    public DomainIpModel get(String domain, String ip, Integer port) {
        DomainIp query = new DomainIp();
        query.setDomain(domain);
        query.setIp(ip);
        query.setPort(port);
        List<DomainIp> domainIps = domainIpRepo.selectPage(query, PageRequest.of(0, 1));
        if (domainIps.size() < 1) {
            return null;
        } else {
            return beanMapper.map(domainIps.get(0), DomainIpModel.class);
        }
    }

    @Override
    public void offline() {
        int batchSize = 100;
        while (true) {
            List<DomainIp> domainIps = domainIpRepo.selectDisable(PageRequest.of(0, batchSize));
            if (domainIps.size() == 0) {
                return;
            }
            logger.info("待下线域名IP资源:{}", JSONObject.toJSONString(domainIps));
            List<Long> ids = Lists.transform(domainIps, new Function<DomainIp, Long>() {
                @Override
                public Long apply(DomainIp input) {
                    return input.getId();
                }
            });
            domainIpRepo.deleteBatch(ids);
        }

    }
}