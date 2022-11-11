package com.virjar.dungproxy.server.service;

import com.virjar.dungproxy.server.model.DomainMetaModel;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DomainMetaService {
    int create(DomainMetaModel domainMetaModel);

    int createSelective(DomainMetaModel domainMetaModel);

    DomainMetaModel findByPrimaryKey(Long id);

    int updateByPrimaryKey(DomainMetaModel domainMetaModel);

    int updateByPrimaryKeySelective(DomainMetaModel domainMetaModel);

    int deleteByPrimaryKey(Long id);

    int selectCount(DomainMetaModel domainMetaModel);

    List<DomainMetaModel> selectPage(DomainMetaModel domainMetaModel, Pageable Pageable);

    List<DomainMetaModel> selectBefore(int days, Pageable pageable);
}