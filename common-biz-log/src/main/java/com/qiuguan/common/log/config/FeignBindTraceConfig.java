package com.qiuguan.common.log.config;

import com.qiuguan.common.log.trace.constants.LogConstants;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fu yuan hui
 * @date 2023-09-27 16:51:46 Wednesday
 */

@Configuration
public class FeignBindTraceConfig {


    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor traceLogRequestInterceptor() {
        return new OpenFeignTraceInterceptor();
    }


    static class OpenFeignTraceInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate requestTemplate) {
            String traceId = MDC.get(LogConstants.TRACE_ID);
            requestTemplate.header(LogConstants.TRACE_ID, traceId);
        }
    }
}
