package com.qiuguan.common.log.trace.interceptor;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuguan.common.log.trace.constants.LogConstants;
import com.qiuguan.common.log.trace.enums.CategoryScopeEnum;
import com.qiuguan.common.log.trace.enums.ModuleType;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import com.qiuguan.common.log.trace.logbiz.service.ServicesLogService;
import com.qiuguan.common.log.utils.ParameterResolveUtils;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author fu yuan hui
 * @date 2023-08-02 17:58:03 Wednesday
 */
@Slf4j
public abstract class AbstractActionExecuteInterceptor implements ActionExecuteInterceptor {

    @Autowired
    protected ServicesLogService servicesLogService;

    @Autowired(required = false)
    protected SqlSessionFactory sqlSessionFactory;

    protected static final String NO_ARGS = null;


    protected void saveLog(ServicesLog servicesLog) {
        this.servicesLogService.save(servicesLog);
    }

    protected ServicesLog createLogBean(LogArgs args) throws Exception {
        ServicesLog servicesLog = new ServicesLog();
        /**
         * build 基础信息
         */
        servicesLog.setCreateTime(LocalDateTime.now());
        servicesLog.setCreatorId(args.getBasicInfo().getCreatorId());
        servicesLog.setCreatorName(args.getBasicInfo().getCreatorName());
        servicesLog.setLogIp(args.getBasicInfo().getIp());
        String mappingUrl = args.getPermissionBean().getMappingUrl();
        if (StringUtils.isNotBlank(mappingUrl) && mappingUrl.contains("{")) {
            //restful风格的url拼起来方便查看
            servicesLog.setUrl(args.getPermissionBean().getMappingUrl() + "=%%=" + args.getUrl());
        } else {
            servicesLog.setUrl(args.getUrl());
        }

        /**
         * 解析参数
         */
        ArgsResolveBean argsResolveBean = resolveArgs(args);
        String jsonArgs = CollectionUtils.isEmpty(argsResolveBean.getReadableArgsMap()) ?
                NO_ARGS : JSON.toJSONString(argsResolveBean.getReadableArgsMap());
        servicesLog.setLogRemark(jsonArgs);
        servicesLog.setSqlParseMap(argsResolveBean.getSqlParseMap());

        /**
         * 其他信息
         */
        String appCode = args.getPermissionBean().getAppCode();
        CategoryScopeEnum categoryScopeEnum = CategoryScopeEnum.matchByCategory(appCode);
        servicesLog.setServicesScope(categoryScopeEnum.getCode());
        servicesLog.setPageName(args.getPermissionBean().getPermissionName());


        servicesLog.setCreateTime(LocalDateTime.now());
        servicesLog.setTraceId(MDC.get(LogConstants.TRACE_ID));
        servicesLog.setModuleType(ModuleType.DAO.getCode());
        return servicesLog;
    }


    protected String resolveSql(LogArgs logArgs, Map<String, Object> paramMap) throws Exception {
        String id = logArgs.getMapperClass().getName() + "." + logArgs.getMethodSignature().getMethod().getName();
        MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);

        BoundSql boundSql = mappedStatement.getBoundSql(paramMap);
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        //去除所有空格，换行符，制表符等特殊字符
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        if (!CollectionUtils.isEmpty(parameterMappings)) {
            TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceAll("\\?", getParamValue(parameterObject));
            } else {
                MetaObject metaObject = sqlSessionFactory.getConfiguration().newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String property = parameterMapping.getProperty();
                    if (metaObject.hasGetter(property)) {
                        Object value = metaObject.getValue(property);
                        sql = sql.replaceFirst("\\?", getParamValue(value));
                    } else if (boundSql.hasAdditionalParameter(property)) {
                        Object additionalParameter = boundSql.getAdditionalParameter(property);
                        sql = sql.replaceFirst("\\?", getParamValue(additionalParameter));
                    }
                }
            }
        }

        return sql;
    }

    protected abstract String resolveReadableSql(String sql);

    protected ArgsResolveBean resolveArgs(LogArgs logArgs) throws Exception {
        ArgsResolveBean argsResolveBean = new ArgsResolveBean();
        Method method = logArgs.getMethodSignature().getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = logArgs.getJoinPoint().getArgs();
        Map<String, Object> sqlParamMap = Maps.newHashMap();
        Map<String, Object> readableParamMap = Maps.newHashMap();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Object arg = args[i];
            String p = parameters[i].getName();
            //无注解参数 @Param
            if (parameterAnnotations[i].length == 0) {
                if (arg instanceof Map) {
                    sqlParamMap.putAll((Map)arg);
                    readableParamMap.putAll((Map)arg);
                } else if (arg instanceof Collection) {
                    readableParamMap.put(p, arg);
                    sqlParamMap.put("list", arg);
                } else {
                    //分页对象不用处理
                    if (!(arg instanceof Page)) {
                        //TODO, MP 批量操作无法拦截
                        Map<String, Object> resolve = ParameterResolveUtils.resolve(p, arg, true);
                        sqlParamMap.putAll(resolve);
                        readableParamMap.putAll(resolve);
                    }
                }
            } else {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof Param) {
                        if (arg instanceof LambdaQueryWrapper
                                || arg instanceof QueryWrapper
                                || arg instanceof LambdaUpdateWrapper
                                || arg instanceof UpdateWrapper) {

                            AbstractWrapper<?, ?, ?> abstractWrapper = (AbstractWrapper<?, ?, ?>) arg;
                            Map<String, Object> paramNameValuePairs = abstractWrapper.getParamNameValuePairs();
                            paramNameValuePairs.forEach((k, v) -> readableParamMap.put(p, v));
                        }

                        sqlParamMap.put(((Param) annotation).value(), ParameterResolveUtils.resolve(p, arg, true));
                    }
                }
            }
        }

        argsResolveBean.setSqlParseMap(sqlParamMap);
        argsResolveBean.setReadableArgsMap(readableParamMap);

        return argsResolveBean;
    }

    protected String getParamValue(Object parameterObject) {
        String value = "null";
        if (parameterObject instanceof String) {
            value = "'" + parameterObject + "'";
        } else if (parameterObject instanceof Date) {
            DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + dateTimeInstance.format((Date) parameterObject) + "'";
        } else {
            if (parameterObject != null) {
                value = parameterObject.toString();
            }
        }

        return value;
    }

    @Data
    protected static class ArgsResolveBean {

        private Map<String, Object> sqlParseMap;

        private Map<String, Object> readableArgsMap;
    }

}
