package com.qiuguan.common.log.config;

import com.qiuguan.common.log.interceptor.GlobalPipelineLogInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author fu yuan hui
 * @date 2023-08-08 19:36:18 Tuesday
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalPipelineLogInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public HandlerInterceptor globalPipelineLogInterceptor(){
        return new GlobalPipelineLogInterceptor();
    }
}
