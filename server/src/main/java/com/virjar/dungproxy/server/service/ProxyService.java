package com.virjar.dungproxy.server.service;

import com.virjar.dungproxy.server.model.ProxyModel;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProxyService {
    int create(ProxyModel proxyModel);

    int createSelective(ProxyModel proxyModel);

    ProxyModel findByPrimaryKey(Long id);

    int updateByPrimaryKey(ProxyModel proxyModel);

    int updateByPrimaryKeySelective(ProxyModel proxyModel);

    int deleteByPrimaryKey(Long id);

    int selectCount(ProxyModel proxyModel);

    List<ProxyModel> selectPage(ProxyModel proxyModel, Pageable Pageable);

    List<ProxyModel> find4availableupdate();

    List<ProxyModel> find4connectionupdate();

    void save(List<ProxyModel> draftproxys);

    ProxyModel selectByIpPort(String ip, int port);

    public List<ProxyModel> allAvailable();

    void offline();
}