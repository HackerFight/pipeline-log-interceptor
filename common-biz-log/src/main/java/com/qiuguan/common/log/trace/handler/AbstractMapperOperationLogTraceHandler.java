package com.qiuguan.common.log.trace.handler;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

/**
 * @author fu yuan hui
 * @date 2023-08-08 18:08:14 Tuesday
 *
 * DAO 层日志记录抽象层
 */
@Slf4j
public abstract class AbstractMapperOperationLogTraceHandler implements MapperOperationLogTraceHandler {


    @Override
    public void trace(final JoinPoint joinPoint, final Object result) {

        try {
            traceLog(joinPoint, result);
        } catch (Exception e) {
            log.error("DAO层日志记录失败, Mapper接口：{}, 方法参数：{}", joinPoint.getTarget(), joinPoint.getArgs(), e);
        }
    }

    protected abstract void traceLog(final JoinPoint joinPoint, final Object result);

}
