package com.hpe.iot.core.nip.adapter.mqtt.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenjial
 *
 */
@Configuration
public class ThreadPoolConfiguration {

	@Value("${subscribe.threadPool.corePoolSize}")
    private int corePoolSize;

    @Bean(name = "subscribeThreadPool")
    public ExecutorService getDefaultSubscribeThreadPool() {
        return new ThreadPoolExecutor(corePoolSize, corePoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    @Bean(name = "sendThreadPool")
    public ExecutorService getDefaultSendThreadPool() {
        return new ThreadPoolExecutor(corePoolSize, corePoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

}
