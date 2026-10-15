package com.bookmyevent.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
public class BackendOperationsConfig {
    @Bean
    Clock bookingClock() {
        return Clock.systemUTC();
    }

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("eventListings");
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(100)
                .expireAfterWrite(Duration.ofSeconds(30)).recordStats());
        return new TransactionAwareCacheManagerProxy(manager);
    }

    @Bean("notificationExecutor")
    Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.setRejectedExecutionHandler((task, pool) -> LoggerFactory.getLogger(
                BackendOperationsConfig.class).warn("action=notification_dropped reason=executor_saturated"));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}