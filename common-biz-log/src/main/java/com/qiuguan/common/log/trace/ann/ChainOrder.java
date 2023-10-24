package com.qiuguan.common.log.trace.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fu yuan hui
 * @date 2023-08-23 11:00:46 Wednesday
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ChainOrder {

    //没有实际意义，就是简单标注一下，越小优先级越高
    int order() default Integer.MAX_VALUE;
}
