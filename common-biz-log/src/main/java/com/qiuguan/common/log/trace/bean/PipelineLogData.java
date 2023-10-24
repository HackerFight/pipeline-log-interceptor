package com.qiuguan.common.log.trace.bean;

import com.qiuguan.common.log.trace.enums.CategoryScopeEnum;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fu yuan hui
 * @date 2023-08-08 18:31:28 Tuesday
 */
@Data
public class PipelineLogData implements Serializable {

    private Long logId;

    private String traceId;

    /**
     * @see CategoryScopeEnum
     */
    //业务日志范畴（菜单级别）：1：组织机构，2：客商管理，3：培训管理，4：财务管理，5：鉴定管理
    private Integer servicesScope;

    //业务范畴（业务类型+报告类型）
    private String businessScope;

    //操作页面（具体页面，修改审批提交保存）
    private String pageName;

    /**
     * @see LogTypeEnum
     */
    private Integer pageFunction;

    //日志明细-入参
    private Object logRemark;

    //日志明细-修改数据
    private Object logUpdateRemark;

    //异常信息
    private String cause;

    //操作ip
    private String logIp;

    //操作人id
    private Integer creatorId;

    //操作人姓名
    private String creatorName;

    //操作人时间
    private LocalDateTime createTime;
}
