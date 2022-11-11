package com.virjar.dungproxy.server.repository;

import com.virjar.dungproxy.server.entity.Proxy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface ProxyRepository {
    int deleteByPrimaryKey(Long id);

    int insert(Proxy proxy);

    int insertSelective(Proxy proxy);

    Proxy selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Proxy proxy);

    int updateByPrimaryKey(Proxy proxy);

    int selectCount(Proxy proxy);

    List<Proxy> selectPage(Proxy proxy, Pageable pageable);

    List<Proxy> getfromSlot(@Param(value = "start") int start, @Param(value = "end") int end,
                            @Param(value = "size") int size, @Param("timeColumnName") String timeColumnName,
                            @Param("scoreColumnName") String scoreColumnName, @Param("condition") String condition);

    Integer getMaxScore(@Param("scoreName") String scoreName);

    Integer getMinScore(@Param("scoreName") String scoreName);

    List<Proxy> findAvailable();

    List<Proxy> find4AreaUpdate(@Param("page") Pageable pageable);

    List<Proxy> find4Distribute(@Param("num") int num, @Param("proxy") Proxy proxy);

    List<Integer> getPortList();

    List<Proxy> getLowProxy(@Param("step") int step, @Param("threshold") int threshold, @Param("page") Pageable pageable);

    List<Proxy> selectByIds(@Param("ids") List<Long> ids);

    Long deleteBatch(@Param("ids") List<Long> ids);

    List<Proxy> selectDisable(@Param("pageable") PageRequest of);
}
//INSERT INTO `proxyipcenter_boot`.`domainip` (`id`, `domain`, `proxy_id`, `ip`, `port`, `domain_score`, `domain_score_date`, `createtime`, `speed`, `test_url`) VALUES (809, 'cx.cnca.cn', 2037, '58.52.80.251', 4331, 1, '2022-11-10 12:54:43', '2022-11-10 12:54:43', NULL, 'http://cx.cnca.cn/CertECloud/index/index/page');