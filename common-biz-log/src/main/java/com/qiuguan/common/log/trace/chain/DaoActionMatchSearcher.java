package com.qiuguan.common.log.trace.chain;

import com.qiuguan.common.log.trace.ann.ChainOrder;
import com.qiuguan.common.log.trace.bean.LogTypePageNameBean;
import com.qiuguan.common.log.trace.bean.PermissionBean;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.handler.RequestMappingMatcherHandler;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fu yuan hui
 * @date 2023-08-23 09:51:25 Wednesday
 */
@ChainOrder(order = 1)
@Slf4j
public class DaoActionMatchSearcher implements LogTypePageNameSearchChain {

    protected volatile Set<String> addKeyWords = ImmutableSet.of("add", "insert", "put", "save", "doSave");

    protected volatile Set<String> updateKeyWords = ImmutableSet.of("update", "modify", "change", "doChange", "doModify");

    private final LogTypePageNameSearchChain logTypePageNameSearchChain;

    private final RequestMappingMatcherHandler requestMappingMatcherHandler;
    private final List<ServicesLog> logs;
    private final PermissionBean p;
    private final Method method;

    public DaoActionMatchSearcher(LogTypePageNameSearchChain logTypePageNameSearchChain,
                                  RequestMappingMatcherHandler requestMappingMatcherHandler,
                                  List<ServicesLog> logs, PermissionBean p, Method method) {
        this.logTypePageNameSearchChain = logTypePageNameSearchChain;
        this.requestMappingMatcherHandler = requestMappingMatcherHandler;
        this.logs = logs;
        this.p = p;
        this.method = method;
    }

    @Override
    public LogTypePageNameBean retrieve() {
        if (!CollectionUtils.isEmpty(logs)) {
            if (logs.size() == 1){
                log.info("执行DAO层动作行为匹配规则，url: {}", p.getMappingUrl());
                //如果匹配到只有一个DAO操作，则以该记录作为CRUD行为的标准
                return new LogTypePageNameBean(LogTypeEnum.matchByCode(logs.get(0).getPageFunction()), getPageName());
            } else {
                //多个DAO操作，但是都是修改，或都是删除，或都是新增，则去重后以唯一的一个作为CRUD标准即可
                List<Integer> collect = logs.stream().map(ServicesLog::getPageFunction).distinct().collect(Collectors.toList());
                if (collect.size() == 1){
                    log.info("执行DAO层动作行为匹配规则，url: {}", p.getMappingUrl());
                    return new LogTypePageNameBean(LogTypeEnum.matchByCode(logs.get(0).getPageFunction()), getPageName());
                } else {
                    //此时无法从DAO层面知道具体的CRUD动作了，如果一个URL只有一个业务逻辑的动作，那么从方法名来适配
                    if (!this.requestMappingMatcherHandler.singleUrlMappingMultiUse(p.getMappingUrl())) {
                        LogTypeEnum logTypeEnum = resolveAnnotation(method);
                        log.info("执行DAO层动作行为匹配规则，url: {}", p.getMappingUrl());
                        return new LogTypePageNameBean(logTypeEnum, getPageName());
                    }
                }
            }
        }

        //继续向下检索
        if (logTypePageNameSearchChain != null) {
            return logTypePageNameSearchChain.retrieve();
        }

        return null;
    }

    private LogTypeEnum resolveAnnotation(Method m) {
        if (AnnotatedElementUtils.hasAnnotation(m, PostMapping.class)) {
            LogTypeEnum logTypeEnum = matchAddOrUpdate(m);
            if (logTypeEnum  == LogTypeEnum.UNKNOWN) {
                return LogTypeEnum.ADD;
            }
            return logTypeEnum;
        }

        if (AnnotatedElementUtils.hasAnnotation(m, PutMapping.class)) {
            return LogTypeEnum.UPDATE;
        }

        if (AnnotatedElementUtils.hasAnnotation(m, DeleteMapping.class)) {
            return LogTypeEnum.DELETE;
        }

        if (AnnotatedElementUtils.hasAnnotation(m, GetMapping.class)) {
            return LogTypeEnum.QUERY;
        }

        if (AnnotatedElementUtils.hasAnnotation(m, RequestMapping.class)) {
            RequestMapping annotation = AnnotationUtils.getAnnotation(m, RequestMapping.class);
            assert annotation != null;
            RequestMethod[] requestMethods = annotation.method();
            for (RequestMethod requestMethod : requestMethods) {
                if (RequestMethod.GET == requestMethod) {
                    return LogTypeEnum.QUERY;
                }

                if (RequestMethod.POST == requestMethod) {
                    LogTypeEnum logTypeEnum = matchAddOrUpdate(m);
                    if (LogTypeEnum.UNKNOWN == logTypeEnum) {
                        return LogTypeEnum.ADD;
                    }

                    return logTypeEnum;
                }

                if (RequestMethod.PUT == requestMethod) {
                    return LogTypeEnum.UPDATE;
                }

                if (RequestMethod.DELETE == requestMethod) {
                    return LogTypeEnum.DELETE;
                }
            }
        }

        return LogTypeEnum.UNKNOWN;
    }

    private String getPageName() {
        String prefix = configPrefix(p.getPermission());
        if (StringUtils.isEmpty(prefix)) {
            return p.getPermissionName();
        }
        return prefix + " - " + p.getPermissionName();
    }


    protected LogTypeEnum matchAddOrUpdate(Method m) {
        for (String addKeyWord : addKeyWords) {
            if (m.getName().startsWith(addKeyWord)) {
                return LogTypeEnum.ADD;
            }
        }

        for (String updateKeyWord : updateKeyWords) {
            if (m.getName().startsWith(updateKeyWord)) {
                return LogTypeEnum.UPDATE;
            }
        }

        return LogTypeEnum.UNKNOWN;
    }
}
