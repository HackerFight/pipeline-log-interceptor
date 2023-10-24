package com.qiuguan.common.log.trace.interceptor;


import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;

/**
 * @author fu yuan hui
 * @date 2023-08-02 17:33:13 Wednesday
 */
public interface ActionExecuteInterceptor {

    boolean supports(LogTypeEnum logTypeEnum);

    void log(LogArgs logArgs) throws Exception;
}
