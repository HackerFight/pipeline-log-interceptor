package com.qiuguan.common.log.trace.bean;

import com.qiuguan.common.log.trace.enums.CategoryScopeEnum;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fu yuan hui
 * @date 2023-08-08 18:30:39 Tuesday
 */
@Data
public class PipelineLogRequestParam implements Serializable {

    /**
     * @see CategoryScopeEnum
     */
    private Integer servicesScope;

    private String pageName;

    private String logRemark;

    /**
     * @see LogTypeEnum
     */
    private Integer pageFunction;

    private String logIp;

    private String traceId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String creatorName;
}
