package com.qiuguan.common.log.trace.filter;


import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author fu yuan hui
 * @date 2023-08-02 16:13:55 Wednesday
 */
@Slf4j
@Component
public class MapperActionMatcherFilter implements ActionMatcherFilter {


    @Autowired(required = false)
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public LogTypeEnum find(Class<?> clazz, Method method) {
        LogTypeEnum logTypeEnum;
        if ((logTypeEnum = findByAnnotation(method)) == LogTypeEnum.UNKNOWN) {
            return findByMybatisConfiguration(clazz, method);
        }
        return logTypeEnum;
    }

    private LogTypeEnum findByMybatisConfiguration(Class<?> mapperClass, Method method) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        final String statement = String.format("%s.%s", mapperClass.getName(), method.getName());
        try {
            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            switch (sqlCommandType) {
                case SELECT:
                    return LogTypeEnum.QUERY;
                case INSERT:
                    return LogTypeEnum.ADD;
                case UPDATE:
                    return LogTypeEnum.UPDATE;
                case DELETE:
                    return LogTypeEnum.DELETE;
                default:
                    return LogTypeEnum.UNKNOWN;
            }
        } catch (Exception e) {
            log.error("查询SELECT|UPDATE|DELETE|INSERT 行为失败", e);
        }
        return LogTypeEnum.UNKNOWN;
    }

    private static LogTypeEnum findByAnnotation(Method method) {
        if (AnnotatedElementUtils.hasAnnotation(method, Select.class) || method.getName().startsWith("select")) {
            return LogTypeEnum.QUERY;
        }

        if (AnnotatedElementUtils.hasAnnotation(method, Update.class) || method.getName().startsWith("update")) {
            return LogTypeEnum.UPDATE;
        }

        if (AnnotatedElementUtils.hasAnnotation(method, Delete.class) || method.getName().startsWith("delete")) {
            return LogTypeEnum.DELETE;
        }

        if (AnnotatedElementUtils.hasAnnotation(method, Insert.class) || method.getName().startsWith("insert")) {
            return LogTypeEnum.ADD;
        }
        return LogTypeEnum.UNKNOWN;
    }
}
