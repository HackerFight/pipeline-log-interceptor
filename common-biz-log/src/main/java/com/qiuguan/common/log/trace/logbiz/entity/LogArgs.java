package com.qiuguan.common.log.trace.logbiz.entity;

import com.qiuguan.common.log.trace.bean.PermissionBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * @author fu yuan hui
 * @date 2023-08-02 17:41:02 Wednesday
 */
@AllArgsConstructor
@Data
public class LogArgs {

    private JoinPoint joinPoint;

    private PermissionBean permissionBean;

    private Object result;

    private BasicLogInfo basicInfo;

    private Class<?> mapperClass;

    private MethodSignature methodSignature;

    private String url;
}
