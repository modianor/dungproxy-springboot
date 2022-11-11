/*
package com.virjar.dungproxy.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class MybatisConfig {
    @Bean("dataSource")
    public DataSource dataSource() {

        */
/**
         *   datasource:
         * #    username: "root"
         * #    password: "2482510236"
         * #    url: jdbc:mysql://localhost:3306/proxyipcenter_boot?useUnicode=true&characterEncoding=utf8&socketTimeout=60000&connectTimeout=30000&serverTimezone=UTC
         * #    hikari:
         * #      minimum-idle: 5
         * #      maximum-pool-size: 10
         * #      idle-timeout: 120000
         * #      max-lifetime: 500000
         * #      leak-detection-threshold: 450000
         * #      pool-name: NotaryDbConnectionPool
         * #      connection-test-query: select 1 from dual
         * #  sql:
         * #    init:
         * #      mode: always
         *//*

        HikariConfig cfg = new HikariConfig();
        // 从池中借出的连接是否默认自动提交事务，默认开启
        cfg.setAutoCommit(true);
        // 从池中获取连接时的等待时间
        cfg.setConnectionTimeout(60000);
        // MYSQL连接相关
        cfg.setJdbcUrl("jdbc:mysql://localhost:3306/proxyipcenter_boot?useUnicode=true&characterEncoding=utf8&socketTimeout=60000&connectTimeout=30000&serverTimezone=UTC");
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setUsername("root");
        cfg.setPassword("2482510236");
        // 连接池的最大容量
        cfg.setMaximumPoolSize(20);
        // 连接池的最小容量，官网不建议设置，保持与 MaximumPoolSize 一致，从而获得最高性能和对峰值需求的响应
        // cfg.setMinimumIdle();
        // 连接池的名称，用于日志监控，多数据源要区分
        cfg.setPoolName("NotaryDbConnectionPool");
        // 池中连接的最长存活时间，要比数据库的 wait_timeout 时间要小不少
        cfg.setMaxLifetime(500000);
        // 连接在池中闲置的最长时间，仅在 minimumIdle 小于 maximumPoolSize 时生效（本配置不生效）
        cfg.setIdleTimeout(120000);
        // 连接泄露检测，默认 0 不开启
        // cfg.setLeakDetectionThreshold();
        // 测试链接是否有效的超时时间，默认 5 秒
        // cfg.setValidationTimeout();
        // MYSQL驱动环境变量
        // 字符编解码
//        cfg.addDataSourceProperty("characterEncoding", );
//        cfg.addDataSourceProperty("useUnicode", );
        // 较新版本的 MySQL 支持服务器端准备好的语句
//        cfg.addDataSourceProperty("useServerPrepStmts", );
        // 缓存SQL开关
//        cfg.addDataSourceProperty("cachePrepStmts", );
        // 缓存SQL数量
//        cfg.addDataSourceProperty("prepStmtCacheSize", );
        // 缓存SQL长度，默认256
        // prepStmtCacheSqlLimit
        return new HikariDataSource(cfg);
    }
}
*/
