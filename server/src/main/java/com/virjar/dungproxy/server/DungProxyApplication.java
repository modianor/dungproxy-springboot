package com.virjar.dungproxy.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
//@ComponentScan(basePackages = {"com.virjar.dungproxy.server.*"})
public class DungProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DungProxyApplication.class, args);
    }

}
