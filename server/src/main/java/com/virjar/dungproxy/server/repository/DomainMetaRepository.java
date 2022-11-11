package com.virjar.dungproxy.server.repository;

import com.virjar.dungproxy.server.entity.DomainMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
public interface DomainMetaRepository {
    int deleteByPrimaryKey(Long id);

    int insert(DomainMeta domainmeta);

    int insertSelective(DomainMeta domainmeta);

    DomainMeta selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DomainMeta domainmeta);

    int updateByPrimaryKey(DomainMeta domainmeta);

    int selectCount(DomainMeta domainmeta);

    List<DomainMeta> selectPage(DomainMeta domainmeta, Pageable pageable);

    List<DomainMeta> selectBefore(@Param("date") Date date, @Param("page") Pageable pageable);
}