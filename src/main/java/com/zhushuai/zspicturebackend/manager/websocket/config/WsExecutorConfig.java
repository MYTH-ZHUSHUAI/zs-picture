package com.zhushuai.zspicturebackend.manager.websocket.config;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * websocket线程池配置
 */
@Configuration
public class WsExecutorConfig {

    @Bean
    public ExecutorService wsSendExecutor() {
        return new ThreadPoolExecutor(
                4,
                20,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}