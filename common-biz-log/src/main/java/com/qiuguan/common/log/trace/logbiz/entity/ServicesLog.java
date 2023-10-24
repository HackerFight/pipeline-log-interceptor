package com.qiuguan.common.log.trace.logbiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qiuguan.common.log.trace.enums.CategoryScopeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 业务日志表(ServicesLog)表实体类
 *
 * @author makejava
 * @since 2023-08-02 11:01:00
 */
@Data
@TableName("services_log")
public class ServicesLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
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
    //操作功能（1-增,2-删,3-改,4-查）
    private Integer pageFunction;
    //日志明细-入参
    private String logRemark;
    //日志明细-修改数据
    private String logUpdateRemark;

    //异常信息
    private String cause;
    //操作ip
    private String logIp;

    //DAO 层 / Controller 层
    private Integer moduleType;

    private String url;

    //操作人id
    private Integer creatorId;
    //操作人姓名
    private String creatorName;
    //操作人时间
    private LocalDateTime createTime;

    @JsonIgnore
    @TableField(exist = false)
    private Map<String, Object> sqlParseMap;

    @JsonIgnore
    @TableField(exist = false)
    private Map<String, Object> readableArgsMap;
}

