package com.virjar.dungproxy.server.repository;

import com.virjar.dungproxy.server.entity.ProxyPolicy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 爬虫代理策略Mapper接口
 *
 * @author ruoyi
 * @date 2022-12-05
 */
@Mapper
public interface ProxyPolicyRepository {
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
     * 删除爬虫代理策略
     *
     * @param id 爬虫代理策略主键
     * @return 结果
     */
    public int deleteProxyPolicyById(Long id);

    /**
     * 批量删除爬虫代理策略
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProxyPolicyByIds(String[] ids);
}
