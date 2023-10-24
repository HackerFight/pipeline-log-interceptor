package com.qiuguan.common.log.trace.handler;

import com.qiuguan.common.log.trace.bean.PermissionBean;
import org.springframework.boot.CommandLineRunner;

import java.lang.reflect.Method;

/**
 * @author fu yuan hui
 * @date 2023-08-01 17:59:50 Tuesday
 */
public interface RequestMappingMatcherHandler extends CommandLineRunner {

    String RESTFUL_PATTERN_PREFIX = "/{";

    String RESTFUL_PATTERN_SUFFIX = "}";

    String PATTERN_BEGIN = "^";

    String PATTERN_END = "$";

    boolean match(String url);

    boolean skip(String url);

    PermissionBean mapping(String url);

    /**
     * 单个url对应多个功能，比如 {@link com.efreight.xn.controller.OrderController#saveSdsOrderIdentify()}
     * @param url
     * @return
     */
    boolean singleUrlMappingMultiUse(String url);

    boolean ignoreAnnotations(Method method);
}
