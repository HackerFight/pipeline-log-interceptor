package com.qiuguan.common.log.utils;

import java.util.UUID;

/**
 * @author fu yuan hui
 * @date 2023-08-05 16:05:24 Saturday
 */
public class MDCUtils {

    /**
     * 目前先用uuid + system.time 来生成trace_id, 如果后续不满足在引入唯一id生成组件
     * @return
     */
    public static String generateTraceId(){
        String prefix = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);
        String suffix = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        return prefix + System.currentTimeMillis() + suffix;
    }
}
