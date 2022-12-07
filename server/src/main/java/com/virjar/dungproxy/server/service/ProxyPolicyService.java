package com.virjar.dungproxy.server.service;


import com.virjar.dungproxy.server.entity.ProxyPolicy;

import java.util.List;

/**
 * 爬虫代理策略Service接口
 *
 * @author ruoyi
 * @date 2022-12-05
 */
public interface ProxyPolicyService {
    /**
     * 查询爬虫代理策略
     *
     * @param id 爬虫代理策略主键
     * @return 爬虫代理策略
     */
    public ProxyPolicy selectProxyPolicyById(Long id);

    /**
     * 查询爬虫代理策略列表
     *
     * @param proxyPolicy 爬虫代理策略
     * @return 爬虫代理策略集合
     */
    public List<ProxyPolicy> selectProxyPolicyList(ProxyPolicy proxyPolicy);

    /**
     * 新增爬虫代理策略
     *
     * @param proxyPolicy 爬虫代理策略
     * @return 结果
     */
    public int insertProxyPolicy(ProxyPolicy proxyPolicy);

    /**
     * 修改爬虫代理策略
     *
     * @param proxyPolicy 爬虫代理策略
     * @return 结果
     */
    public int updateProxyPolicy(ProxyPolicy proxyPolicy);

    /**
     * 批量删除爬虫代理策略
     *
     * @param ids 需要删除的爬虫代理策略主键集合
     * @return 结果
     */
    public int deleteProxyPolicyByIds(String ids);

    /**
     * 删除爬虫代理策略信息
     *
     * @param id 爬虫代理策略主键
     * @return 结果
     */
    public int deleteProxyPolicyById(Long id);
}
