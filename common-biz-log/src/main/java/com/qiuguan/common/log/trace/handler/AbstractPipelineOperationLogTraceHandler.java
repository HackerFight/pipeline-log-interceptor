package com.qiuguan.common.log.trace.handler;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

import java.util.Arrays;

/**
 * @author fu yuan hui
 * @date 2023-08-05 16:43:56 Saturday
 *
 * 全链路日志记录 抽象层
 */
@Slf4j
public abstract class AbstractPipelineOperationLogTraceHandler implements PipelineOperationLogTraceHandler {

    protected static final String RESULT_CODE_KEY = "code";

    protected static final String RESULT_MSG_KEY = "message";

    protected static final String RESULT_DEFAULT_MSG = "success";

    protected static final String EXCEPTION_MSG = "异常信息";

    abstract void resolveTraceLog(final JoinPoint joinPoint,  final Object result);

    @Override
    public void traceLog(final JoinPoint joinPoint, final Object result) {
        try {

            resolveTraceLog(joinPoint, result);

        } catch (Exception e) {
            log.error("全链路日志记录失败, 目标对象: {}, 请求方法:{}, 方法参数:{}", joinPoint.getTarget(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()), e);
        }
    }
}
