package com.qiuguan.common.log.aspect;

import cn.hutool.core.util.ObjectUtil;
import com.qiuguan.common.log.trace.handler.PipelineOperationLogTraceHandler;
import com.qiuguan.common.log.utils.IpUtils;
import com.sun.nio.sctp.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 18368
 */
@Slf4j
@AllArgsConstructor
@Aspect
@Component
@Order(1)
public class WebLogAspect {

    private final PipelineOperationLogTraceHandler pipelineLogTraceHandler;

    @Pointcut("execution(* com.qiuguan.*.controller.*.*(..)) || execution(* com.qiuguan.*.api.*.*(..))")
    public void aspect() {
    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch("全链路请求耗时统计");
        String methodName = "";
        try {
            Signature signature = joinPoint.getSignature();
            methodName = signature.getName();
            stopWatch.start(methodName);
            Object proceed = joinPoint.proceed(joinPoint.getArgs());
            stopWatch.stop();
            stopWatch.start("async-log");
            this.pipelineLogTraceHandler.traceLog(joinPoint, proceed);
            stopWatch.stop();
            return proceed;
        } catch (Throwable e) {
            log.error(">>>>>-----------请求异常-->请求方法：{}, ", methodName, e);
            if(stopWatch.isRunning()) {
                stopWatch.stop();
            }
            stopWatch.start("async-log-throwable");
            this.pipelineLogTraceHandler.exceptionLog(joinPoint, e);
            stopWatch.stop();
            throw new RuntimeException(e);
        } finally {
            if(stopWatch.isRunning()){
                stopWatch.stop();
            }
            log.info("\n方法执行耗时统计：{}", stopWatch.prettyPrint());
        }
    }

    @Before("aspect()")
    public void beforeWeblog(JoinPoint joinPoint) throws Throwable {
        try {

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();

            Signature signature = joinPoint.getSignature();
            log.info(">>>>>-----------请求类名:" + signature.getDeclaringTypeName());
            log.info(">>>>>-----------执行方法:" + signature.getName());
            log.info(">>>>>-----------请求地址:" + request.getRequestURL() + " ------------" + request.getMethod());
            log.info(">>>>>-----------请求IP:" + IpUtils.getIpAddr(request));
            String params = "";
            Object[] obj = joinPoint.getArgs();
            for (Object object : obj) {
                params = params + object + "|";
            }
            log.info(">>>>>-----------请求参数:" + params);
        } catch (Exception e) {
            log.error("请求参数转义失败", e);
            log.warn(">>>>>-----------请求参数转义失败，无法打印！！！");
            throw new RuntimeException(getStrackTrace(e.getCause()));
        }
    }

    @AfterReturning(returning = "respObj", pointcut = "aspect()")
    public void aferWebLog(Object respObj) throws Throwable {
        // 返回页面
        if (respObj instanceof MessageInfo) {
            MessageInfo view = (MessageInfo) respObj;
            Map<String, Object> modelMap = new HashMap<>();
            BeanUtils.populate(view, modelMap);
            log.debug(">>>>>-----------返回结果：" + modelMap.toString());
        } else {
            log.debug(">>>>>-----------返回结果：" + ObjectUtil.toString(respObj));
        }
    }


    /**
     * 获取异常堆栈信息
     *
     * @param throwable
     * @return
     */
    public static String getStrackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }
}
