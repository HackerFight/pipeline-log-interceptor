package com.qiuguan.common.log.trace.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fu yuan hui
 * @date 2023-08-02 10:34:27 Wednesday
 */
@AllArgsConstructor
@Getter
public enum LogTypeEnum {

    /* ---------------  common ---------------------*/

    ADD(1, "新增"),

    DELETE(2, "删除"),

    UPDATE(3, "修改"),

    QUERY(4, "查询"),

    IMPORT(5, "导入"),

    EXPORT(6, "导出"),

    /* --------------- xn ---------------------------*/

    APPROVE(7, "审批"),

    IDENTIFY(8, "识别"),

    TECHNOLOGY(9, "技术"),

    FIRST_APPROVE(10, "初审"),

    TECH_UPDATE(11, "技术更新"),

    SERVICE_UPDATE(12, "客服更新"),

    /* ------------------- css -------------------------*/

    APPLY_DEBIT_BILL(3, "账单开票申请"),

    APPLY_STATEMENT_BILL(3, "清单开票申请"),

    DEBIT_WRITE_OFF(3, "账单核销"),

    STATEMENT_WRITE_OFF(3, "清单核销"),

    DEBIT_BILL(3, "账单开票"),

    STATEMENT_BILL(3, "清单开票"),

    CANCEL_DEBIT_BILL_APPLY(3, "退回-撤销账单开票申请"),

    CANCEL_STATEMENT_BILL_APPLY(3, "退回-撤销清单开票申请"),

    UPLOAD(5, "上传附件"),


    /* -------------------- fallback -----------------------------*/

    UNKNOWN(-1, "未知");

    private final Integer code;

    private final String action;


    public static LogTypeEnum matchByCode(Integer code) {
        LogTypeEnum[] values = values();
        for (LogTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
