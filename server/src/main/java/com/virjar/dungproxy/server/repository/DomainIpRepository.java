package com.virjar.dungproxy.server.repository;

import com.virjar.dungproxy.server.entity.DomainIp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface DomainIpRepository {
    int deleteByPrimaryKey(Long id);

    int insert(DomainIp domainip);

    int insertSelective(DomainIp domainip);

    DomainIp selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DomainIp domainip);

    int updateByPrimaryKey(DomainIp domainip);

    int selectCount(DomainIp domainip);

    int selectAvaCount(DomainIp domainip);

    List<DomainIp> selectPage(DomainIp domainip, Pageable pageable);

    List<DomainIp> selectAvailable(@Param("domain") String domain, @Param("pageable") Pageable pageable);

    List<DomainIp> selectDisable(@Param("pageable") Pageable pageable);

    int deleteBatch(@Param("ids") List<Long> ids);

    int deleteBatchByProxyId(@Param("ids") List<Long> ids);

    int deleteByDomain(@Param("domain") String domain);
}