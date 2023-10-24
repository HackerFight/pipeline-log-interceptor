package com.qiuguan.common.log.trace.handler;


import com.qiuguan.common.log.trace.ann.GlobalLogIgnore;
import com.qiuguan.common.log.trace.bean.PermissionBean;
import com.qiuguan.common.log.trace.constants.LogConstants;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.filter.ActionMatcherFilter;
import com.qiuguan.common.log.trace.interceptor.ActionExecuteInterceptorComposite;
import com.qiuguan.common.log.trace.logbiz.dao.ServicesLogMapper;
import com.qiuguan.common.log.trace.logbiz.entity.BasicLogInfo;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import com.qiuguan.common.log.utils.EUserDetails;
import com.qiuguan.common.log.utils.IpUtils;
import com.qiuguan.common.log.utils.ParameterResolveUtils;
import com.qiuguan.common.log.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author fu yuan hui
 * @date 2023-08-01 18:58:18 Tuesday
 *
 * DAO层日志记录默认实现
 */
@Slf4j
@AllArgsConstructor
@Component
public class DefaultMapperLogTraceHandler extends AbstractMapperOperationLogTraceHandler {

    private final RequestMappingMatcherHandler requestMappingMatcherHandler;

    private final ThreadPoolTaskExecutor asyncLogTaskExecutor;

    private final ActionMatcherFilter actionMatcherFilter;

    private final ActionExecuteInterceptorComposite interceptorComposite;


    /**
     * DAO层操作的日志记录
     */
    @Override
    protected void traceLog(final JoinPoint joinPoint, final Object result) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return;
        }

        //异步记录日志
        asyncLogTaskExecutor.execute(() -> {
            try {
                MethodSignature ms = (MethodSignature) joinPoint.getSignature();
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                final String url = request.getRequestURI();
                if (AnnotatedElementUtils.hasAnnotation(ms.getMethod(), GlobalLogIgnore.class) ||
                        !requestMappingMatcherHandler.match(url)
                        || ParameterResolveUtils.advanceCheckSelectAnnotation(ms.getMethod())
                        || this.requestMappingMatcherHandler.skip(url)) {
                    return;
                }

                //得到原始类型的Mapper接口
                final Class<?>[] classes = AopProxyUtils.proxiedUserInterfaces(joinPoint.getTarget());
                if (ArrayUtils.isEmpty(classes)) {
                    log.warn("trace log handler not found match mapper, url: {}", url);
                    return;
                }
                //记录日志的Mapper本身不需要记录，业务模块也有记录日志的Mapper,其实也需要跳过，可以通过反射获取
                final Class<?> mapperClass = classes[0];
                if (null == mapperClass || ServicesLogMapper.class == mapperClass) {
                    return;
                }

                final BasicLogInfo basicLogInfo = createBasicLogInfo();
                LogTypeEnum logTypeEnum = actionMatcherFilter.find(mapperClass, ms.getMethod());
                if (logTypeEnum == LogTypeEnum.UNKNOWN || logTypeEnum == LogTypeEnum.QUERY) {
                    log.warn("trace log handler not found match log type or query, unable record log, url: {}, curd: {}", url, logTypeEnum.getAction());
                    return;
                }
                PermissionBean mapping = requestMappingMatcherHandler.mapping(url);
                if (null == mapping) {
                    return;
                }

                final LogArgs args = new LogArgs(joinPoint, mapping, result, basicLogInfo, mapperClass, ms, url);
                interceptorComposite.handle(args, logTypeEnum);

            } catch (Exception e) {
                log.error("异步记录DAO层日志失败", e);
            } finally {
                //子线程的trace_id也确保要移除掉
                MDC.remove(LogConstants.TRACE_ID);
            }
        });
    }

    private BasicLogInfo createBasicLogInfo() {
        BasicLogInfo basicLogInfo = new BasicLogInfo();
        EUserDetails user = SecurityUtils.getUser();
        if (!Objects.isNull(user)) {
            basicLogInfo.setCreatorName(user.getUserCname());
            basicLogInfo.setCreatorId(user.getId());
        } else {
            basicLogInfo.setCreatorName("unknown");
            basicLogInfo.setCreatorId(-9999);
        }
        basicLogInfo.setIp(IpUtils.getIpAddr());
        return basicLogInfo;
    }

}
