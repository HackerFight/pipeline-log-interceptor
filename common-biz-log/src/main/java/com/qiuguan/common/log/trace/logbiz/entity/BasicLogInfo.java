package com.qiuguan.common.log.trace.logbiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fu yuan hui
 * @date 2023-08-02 18:06:46 Wednesday
 */
@Data
final public class BasicLogInfo {

    private volatile String creatorName;

    private volatile Integer creatorId;

    private volatile LocalDateTime createTime;

    private volatile String ip;
}
