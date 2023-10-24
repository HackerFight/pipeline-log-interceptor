package com.qiuguan.common.log.trace.handler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.R;
import com.qiuguan.common.log.trace.ann.GlobalLogIgnore;
import com.qiuguan.common.log.trace.bean.LogTypePageNameBean;
import com.qiuguan.common.log.trace.bean.PermissionBean;
import com.qiuguan.common.log.trace.chain.DaoActionMatchSearcher;
import com.qiuguan.common.log.trace.chain.LogTypePageNameSearchChain;
import com.qiuguan.common.log.trace.chain.MappingUrlMarkSearcher;
import com.qiuguan.common.log.trace.chain.SingletonUrlMappingMultiUseSearcher;
import com.qiuguan.common.log.trace.constants.LogConstants;
import com.qiuguan.common.log.trace.enums.CategoryScopeEnum;
import com.qiuguan.common.log.trace.enums.ModuleType;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import com.qiuguan.common.log.trace.logbiz.service.ServicesLogService;
import com.qiuguan.common.log.utils.IpUtils;
import com.qiuguan.common.log.utils.ParameterResolveUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author fu yuan hui
 * @date 2023-08-04 16:52:00 Friday
 *
 *
 * 全链路日志记录 默认实现
 */
@AllArgsConstructor
@Slf4j
@Component
public class DefaultPipelineLogRecordProcessor extends AbstractPipelineOperationLogTraceHandler {

    private final RequestMappingMatcherHandler requestMappingMatcherHandler;

    private final ThreadPoolTaskExecutor asyncLogTaskExecutor;

    private final ServicesLogService servicesLogService;

    @Override
    void resolveTraceLog(final JoinPoint joinPoint, final Object result) {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return;
        }

        //异步记录日志
        asyncLogTaskExecutor.execute(() -> {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest req = attributes.getRequest();
                final String reqUrl = req.getRequestURI();
                final Method m = ((MethodSignature) joinPoint.getSignature()).getMethod();
                if (AnnotatedElementUtils.hasAnnotation(m, GlobalLogIgnore.class)
                        || ParameterResolveUtils.shouldSkip(m)
                        || !requestMappingMatcherHandler.match(reqUrl)
                        || requestMappingMatcherHandler.ignoreAnnotations(m)
                        || requestMappingMatcherHandler.skip(reqUrl)) {
                    log.warn("trace log processor not found match request url or skip url, unable record log: {}, {}", reqUrl, m.getName());
                    return;
                }

                PermissionBean mapping = requestMappingMatcherHandler.mapping(reqUrl);
                if (mapping == null) {
                    return;
                }

                List<ServicesLog> logs = servicesLogService.findByTraceId(MDC.get(LogConstants.TRACE_ID));
                if (CollectionUtils.isEmpty(logs)) {
                    //上面如果查询不到，有可能是因为DAO层操作比较耗时，这里等1s
                    TimeUnit.MILLISECONDS.sleep(2000);
                    logs = servicesLogService.findByTraceId(MDC.get(LogConstants.TRACE_ID));
                }

                if (CollectionUtils.isEmpty(logs)) {
                    log.warn("...not found database operations, skip log record...");
                    return;
                }

                //检索匹配规则
                LogTypePageNameBean retrieve = createSearchChain(mapping, m, logs, joinPoint.getArgs(), reqUrl).retrieve();

                List<String> opertionList = logs.stream().map(ServicesLog::getLogUpdateRemark)
                        .filter(StringUtils::isNotBlank).collect(Collectors.toList());

                ServicesLog serviceLog = logs.get(0);
                serviceLog.setLogRemark(resolveArgs(m, joinPoint.getArgs()));
                serviceLog.setLogUpdateRemark(CollectionUtils.isEmpty(opertionList) ? null : opertionList.toString());
                serviceLog.setCause(resolveResultMessage(result));
                serviceLog.setPageName(retrieve.getPageName());
                serviceLog.setServicesScope(CategoryScopeEnum.matchByCategory(mapping.getAppCode()).getCode());
                serviceLog.setTraceId(MDC.get(LogConstants.TRACE_ID));
                serviceLog.setPageFunction(retrieve.getLogTypeEnum().getCode());
                serviceLog.setBusinessScope("全链路");
                serviceLog.setModuleType(ModuleType.CONTROLLER.getCode());

                //将DAO层日志移除，组成Controller层一条日志
                //servicesLogService.removeLogsByTraceId(MDC.get(LogConstants.TRACE_ID));
                servicesLogService.save(serviceLog);

            } catch (Exception e) {
                log.error("异步记录全链路日志失败", e);
            } finally {
                MDC.remove(LogConstants.TRACE_ID);
            }
        });
    }

    @Override
    public void exceptionLog(JoinPoint joinPoint, Throwable throwable) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;

        //记录记录异常日志
        CompletableFuture.runAsync(() -> {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest req = attributes.getRequest();
                final String reqUrl = req.getRequestURI();
                final Method m = ((MethodSignature)joinPoint.getSignature()).getMethod();

                if (AnnotatedElementUtils.hasAnnotation(m, GlobalLogIgnore.class)
                        || !requestMappingMatcherHandler.match(reqUrl)
                        || requestMappingMatcherHandler.ignoreAnnotations(m)
                        || ParameterResolveUtils.shouldSkip(m)
                        || requestMappingMatcherHandler.skip(reqUrl)) {
                    log.warn("trace log handler not found match request url or skip url, unable record log: {}", reqUrl);
                    return;
                }

                final PermissionBean  mapping = requestMappingMatcherHandler.mapping(reqUrl);
                if(null == mapping) {
                    return;
                }

                //匹配规则检索
                LogTypePageNameBean retrieve = createSearchChain(mapping, m, null, joinPoint.getArgs(), reqUrl).retrieve();

                final ServicesLog servicesLog = createServiceLog();
                servicesLog.setPageName(retrieve.getPageName());
                servicesLog.setBusinessScope("全局异常");
                servicesLog.setCause(resolveException(throwable));
                servicesLog.setServicesScope(CategoryScopeEnum.matchByCategory(mapping.getAppCode()).getCode());
                servicesLog.setPageFunction(retrieve.getLogTypeEnum().getCode());
                servicesLog.setLogRemark(resolveArgs(joinPoint));
                servicesLog.setTraceId(MDC.get(LogConstants.TRACE_ID));
                servicesLog.setModuleType(ModuleType.CONTROLLER.getCode());
                servicesLog.setUrl(reqUrl);

                servicesLogService.save(servicesLog);

            } catch (Exception e) {
                log.error("全链路全局异常信息记录失败， {}， {}", joinPoint.getTarget(), joinPoint.getSignature().getName(), e);
            } finally {
                MDC.remove(LogConstants.TRACE_ID);
            }

        }, asyncLogTaskExecutor);
    }


    private LogTypePageNameSearchChain createSearchChain(PermissionBean p, Method method, List<ServicesLog> logs, Object[] args, String url) {
        LogTypePageNameSearchChain chain1 = new SingletonUrlMappingMultiUseSearcher(null, p);
        LogTypePageNameSearchChain chain2 = new DaoActionMatchSearcher(chain1, requestMappingMatcherHandler, logs, p, method);
        return new MappingUrlMarkSearcher(chain2, p, args, url);
    }

    private String resolveArgs(final JoinPoint joinPoint) throws Exception {
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        return ParameterResolveUtils.resolveArgs(method, args, false);
    }

    private String resolveException(final Throwable ex) {
        Map<String, Object> messageMap = Maps.newConcurrentMap();
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException e = (MethodArgumentNotValidException) ex;
            Map<String, String> argumentMap = Maps.newConcurrentMap();
            List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                argumentMap.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            messageMap.put(EXCEPTION_MSG, argumentMap);
        } else {
            messageMap.put(EXCEPTION_MSG, ex.toString());
        }

        return JSON.toJSONString(messageMap);
    }


    private String resolveResultMessage(Object result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT_MSG_KEY, RESULT_DEFAULT_MSG);
        if (result instanceof MessageInfo) {
            resultMap.put(RESULT_CODE_KEY, ((MessageInfo<?>) result).getCode());
            resultMap.put(RESULT_MSG_KEY, ((MessageInfo<?>) result).getMsg());
        }

        if (result instanceof R) {
            resultMap.put(RESULT_MSG_KEY, ((R<?>) result).getMsg());
            resultMap.put(RESULT_CODE_KEY, ((R<?>) result).getCode());
        }

        return JSON.toJSONString(resultMap);
    }

    protected String resolveArgs(Method method, Object[] args) throws Exception {
        return ParameterResolveUtils.resolveArgs(method, args, false);
    }

    protected ServicesLog createServiceLog(){
        ServicesLog log = new ServicesLog();
        log.setCreatorId(SecurityUtils.getUser().getId());
        log.setCreatorName(SecurityUtils.getUser().getUserCname());
        log.setCreateTime(LocalDateTime.now());
        log.setLogIp(IpUtils.getIpAddr());
        return log;
    }
}
