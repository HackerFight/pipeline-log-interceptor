package com.qiuguan.common.log.aspect;

import com.qiuguan.common.log.trace.handler.MapperOperationLogTraceHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author fu yuan hui
 * @date 2023-08-02 14:57:52 Wednesday
 */
@Slf4j
@AllArgsConstructor
@Aspect
@Component
public class MapperDataLogAspect {

    private final MapperOperationLogTraceHandler mapperOperationLogTraceHandler;

    /**
     * service层直调 {@link com.baomidou.mybatisplus.core.mapper.BaseMapper } 接口方法也会被拦截
     */
    @Pointcut("execution(* com.qiuguan.*.dao.*.*(..))")
    public void aspect() {}


    @Around("aspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String method = signature.getName();
        try {
            Object result = joinPoint.proceed(joinPoint.getArgs());
            //DAO层记录成功这里才需要进行日志记录
            this.mapperOperationLogTraceHandler.trace(joinPoint, result);
            return result;
        } catch (Throwable e) {
            log.error(">>>>>-----------请求异常-->请求方法 : {}", method, e);
            return new Object();
        }
    }
}
