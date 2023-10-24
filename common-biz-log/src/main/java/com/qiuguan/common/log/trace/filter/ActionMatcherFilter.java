package com.qiuguan.common.log.trace.filter;


import com.qiuguan.common.log.trace.enums.LogTypeEnum;

import java.lang.reflect.Method;

/**
 * @author fu yuan hui
 * @date 2023-08-02 11:01:40 Wednesday
 */
public interface ActionMatcherFilter {

    LogTypeEnum find(Class<?> clazz, Method method);
}
