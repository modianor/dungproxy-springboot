package com.virjar.dungproxy.server.repository;

import com.virjar.dungproxy.server.entity.Proxy;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Created by nicholas on 9/19/2016.
 */
@Mapper
public interface ProxyLowQualityRepository {
    int insert(Proxy proxy);

    int insertSelective(Proxy proxy);

    Proxy isExists(Long id);
}
