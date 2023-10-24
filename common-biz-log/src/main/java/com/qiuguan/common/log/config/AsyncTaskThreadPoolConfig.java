package com.qiuguan.common.log.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fu yuan hui
 * @date 2023-08-02 10:21:26 Wednesday
 */
@Configuration
public class AsyncTaskThreadPoolConfig {

    @Bean(name = "asyncLogTaskExecutor")
    public ThreadPoolTaskExecutor asyncLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() << 1);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() << 2);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("async-log-task-");
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    @Slf4j
    static class ContextCopyingDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            try {
                RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
                Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                SecurityContext securityContext = SecurityContextHolder.getContext();
                return () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes, true);
                        MDC.setContextMap(copyOfContextMap);
                        SecurityContextHolder.setContext(securityContext);

                        runnable.run();

                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                        MDC.clear();
                        SecurityContextHolder.clearContext();
                    }
                };
            } catch (IllegalStateException e) {
                log.error("包装线程执行失败, 请检查", e);
            }

            return runnable;
        }
    }
}
