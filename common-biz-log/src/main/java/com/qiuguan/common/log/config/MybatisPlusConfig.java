package com.qiuguan.common.log.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author fu yuan hui
 * @date 2023-08-02 13:42:39 Wednesday
 */
@MapperScan("com.efreight.common.log.trace.logbiz.dao")
@Configuration
public class MybatisPlusConfig {

}
