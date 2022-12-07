package com.virjar.dungproxy.server.service.impl;

import com.virjar.dungproxy.server.entity.ProxyPolicy;
import com.virjar.dungproxy.server.repository.ProxyPolicyRepository;
import com.virjar.dungproxy.server.service.ProxyPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 爬虫代理策略Service业务层处理
 *
 * @author ruoyi
 * @date 2022-12-05
 */
@Service
public class ProxyPolicyServiceImpl implements ProxyPolicyService {
    @Autowired
    private ProxyPolicyRepository proxyPolicyRepository;

    /**
     * 查询爬虫代理策略
     *
     * @param id 爬虫代理策略主键
     * @return 爬虫代理策略
     */
    @Override
    public ProxyPolicy selectProxyPolicyById(Long id) {
        return proxyPolicyRepository.selectProxyPolicyById(id);
    }

    /**
     * 查询爬虫代理策略列表
     *
     * @param proxyPolicy 爬虫代理策略
     * @return 爬虫代理策略
     */
    @Override
    public List<ProxyPolicy> selectProxyPolicyList(ProxyPolicy proxyPolicy) {
        return proxyPolicyRepository.selectProxyPolicyList(proxyPolicy);
    }

    /**
     * 新增爬虫代理策略
     *
     * @param proxyPolicy 爬虫代理策略
     * @return 结果
     */
    @Override
    public int insertProxyPolicy(ProxyPolicy proxyPolicy) {
        return proxyPolicyRepository.insertProxyPolicy(proxyPolicy);
    }

    /**
     * 修改爬虫代理策略
     *
     * @param proxyPolicy 爬虫代理策略
     * @return 结果
     */
    @Override
    public int updateProxyPolicy(ProxyPolicy proxyPolicy) {
        return proxyPolicyRepository.updateProxyPolicy(proxyPolicy);
    }

    /**
     * 批量删除爬虫代理策略
     *
     * @param ids 需要删除的爬虫代理策略主键
     * @return 结果
     */
    @Override
    public int deleteProxyPolicyByIds(String ids) {
        return -1;
    }

    /**
     * 删除爬虫代理策略信息
     *
     * @param id 爬虫代理策略主键
     * @return 结果
     */
    @Override
    public int deleteProxyPolicyById(Long id) {
        return proxyPolicyRepository.deleteProxyPolicyById(id);
    }
}
