package com.qiuguan.common.log.interceptor;

import com.qiuguan.common.log.trace.constants.LogConstants;
import com.qiuguan.common.log.utils.MDCUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fu yuan hui
 * @date 2023-08-08 21:34:44 Tuesday
 *
 * 请求 ---> 网关 ---过滤器 ---拦截器(preHandle方法) --- AOP --- 全局异常
 *  ^                                                           |
 *  |                                                           |
 *  —————————————————————拦截器(afterCompletion方法)<—————————————-
 *
 */
@Slf4j
public class GlobalPipelineLogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId;
        if (!StringUtils.isEmpty(traceId = request.getHeader(LogConstants.TRACE_ID))
                || !StringUtils.isEmpty(traceId = MDC.get(LogConstants.TRACE_ID))) {
            MDC.put(LogConstants.TRACE_ID, traceId);
        } else {
            MDC.put(LogConstants.TRACE_ID, MDCUtils.generateTraceId());
        }

        MDC.put(LogConstants.LOG_URI, request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            log.info("execute global pipeline log handlerInterceptor afterCompletion method and remove trace_id");
        }finally {
            MDC.remove(LogConstants.TRACE_ID);
            MDC.remove(LogConstants.LOG_URI);
        }
    }
}
