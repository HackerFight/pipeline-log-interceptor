package com.qiuguan.common.log.trace.handler;

import org.aspectj.lang.JoinPoint;

/**
 * @author fu yuan hui
 * @date 2023-08-01 17:47:23 Tuesday
 *
 * DAO 层日志记录 根接口
 *
 * @see AbstractMapperOperationLogTraceHandler
 * @see DefaultMapperLogTraceHandler
 */
public interface MapperOperationLogTraceHandler {
    /**
     * 异步记录DAO日志
     */
    void trace(final JoinPoint joinPoint, final Object result);

}