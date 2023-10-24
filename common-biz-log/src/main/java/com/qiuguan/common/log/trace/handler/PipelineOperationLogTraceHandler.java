package com.qiuguan.common.log.trace.handler;

import org.aspectj.lang.JoinPoint;

/**
 * @author fu yuan hui
 * @date 2023-08-04 16:52:00 Friday
 *
 *
 * 全链路日志记录 根接口
 */
public interface PipelineOperationLogTraceHandler {

    /**
     * @param joinPoint
     * @param result
     */
    void traceLog(final JoinPoint joinPoint, final Object result);

    void exceptionLog(final JoinPoint joinPoint, final Throwable throwable);
}
