package com.qiuguan.common.log.trace.interceptor;

import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fu yuan hui
 * @date 2023-08-02 17:35:00 Wednesday
 */
@Slf4j
@AllArgsConstructor
@Component
public class ActionExecuteInterceptorComposite {

    private final List<ActionExecuteInterceptor> interceptors;

    public void handle(LogArgs args, LogTypeEnum logTypeEnum) {
        try {
            for (ActionExecuteInterceptor interceptor : this.interceptors) {
                if (interceptor.supports(logTypeEnum)) {
                    interceptor.log(args);
                }
            }
        } catch (Exception e) {
            log.error("日志记录失败，args:{}, logTypeEnum:{}", args, logTypeEnum, e);
        }
    }

}
