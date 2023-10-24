package com.qiuguan.common.log.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fu yuan hui
 * @date 2023-08-06 17:20:57 Sunday
 */
public class ParameterResolveUtils {

    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final String BASE_64 = "base64";
    public static String resolveArgs(Method method, Object[] args, boolean beanPropertyResolve) throws Exception {
        Parameter[] parameters = method.getParameters();

        String[] parameterNames = DISCOVERER.getParameterNames(method);
        Map<String, Object> paramMap = new HashMap<>();
        if (!ArrayUtils.isEmpty(parameters)) {
            for (int i = 0; i < parameters.length; i++) {
                Object argsVal = args[i];
                if (argsVal instanceof HttpServletRequest
                        || argsVal instanceof HttpServletResponse
                        || argsVal instanceof HttpSession
                        || argsVal instanceof Writer
                        || argsVal instanceof MultipartFile
                        || argsVal instanceof Page) {
                    continue;
                }

                String name = parameters[i].getName();
                if (!argsVal.getClass().isInterface() && !ArrayUtils.isEmpty(parameterNames)) {
                    name = parameterNames[i];
                }

                if(argsVal instanceof Collection) {
                    return JSON.toJSONString(argsVal);
                }

                paramMap.putAll(resolve(name, argsVal, beanPropertyResolve));
            }
        }

        return CollectionUtils.isEmpty(paramMap) ? null : JSON.toJSONString(paramMap);
    }

    public static Map<String,Object> resolve(String paramName, Object arg, boolean beanPropertyResolve) throws IllegalAccessException {
        Map<String, Object> paramMap = Maps.newHashMap();
        if (null == arg) {
            return paramMap;
        }
        Class<?> c = arg.getClass();
        //基本数据类型或者包装类,或者日期类型就不用解析了
        if (isSimpleType(c)) {
            paramMap.put(paramName, arg);
            return paramMap;
        }

        if (paramName.contains(BASE_64)) {
            paramMap.put(paramName, null);
            return paramMap;
        }

        if (arg instanceof String) {
            String cs = (String) arg;
            if (cs.length() > 3000) {
                paramMap.put(paramName, null);
            }

            paramMap.put(paramName, arg);

            return paramMap;
        }

        if (arg instanceof Map) {
            paramMap.put(paramName, JSON.toJSONString(arg));
            return paramMap;
        }

        //自定义对象
        if (arg.getClass().getClassLoader() != null && beanPropertyResolve) {
            List<Field> fieledList = Lists.newArrayList();
            Class<?> aClass = arg.getClass();
            while (aClass != null) {
                fieledList.addAll(Arrays.asList(aClass.getDeclaredFields()));
                aClass = aClass.getSuperclass();
            }

            for (Field field : fieledList) {
                field.setAccessible(true);
                String name = field.getName();
                Object v = field.get(arg);
                if (name.contains(BASE_64)) {
                    paramMap.put(name, null);
                } else if (v instanceof String) {
                    String vs = (String) v;
                    if (vs.length() > 3000) {
                        paramMap.put(name, null);
                    } else {
                        paramMap.put(name, vs);
                    }
                } else {
                    paramMap.put(name, v);
                }
            }
        } else {
            String jsonValue;
            try {
                //分页对象过滤掉
                if (arg instanceof IPage) {
                    return paramMap;
                }
                jsonValue = JSON.toJSONString(arg);
            } catch (Exception e) {
                jsonValue = arg.toString();
            }

            paramMap.put(paramName, jsonValue);
        }

        return paramMap;
    }


    public static boolean advanceCheckSelectAnnotation(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, Select.class)  || method.getName().startsWith("select");
    }

    public static boolean shouldSkip(Method method) {
        boolean has = AnnotatedElementUtils.hasAnnotation(method, GetMapping.class);
        if(!has && AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
            RequestMapping annotation = AnnotationUtils.getAnnotation(method, RequestMapping.class);
            RequestMethod[] m = annotation.method();
            if (ArrayUtils.isEmpty(m)) {
                return false;
            }
            return Arrays.stream(m).anyMatch(x -> x == RequestMethod.GET);
        }

        return has;
    }

    public static boolean isSimpleType(Class<?> c){
        return c.isPrimitive() || isWrapClass(c) || LocalDate.class.isAssignableFrom(c)
                || LocalDateTime.class.isAssignableFrom(c) || Date.class.isAssignableFrom(c);
    }

    public static boolean isWrapClass(Class<?> aClass) {
        try {
            return ((Class<?>) aClass.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
