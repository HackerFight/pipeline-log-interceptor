package com.qiuguan.common.log.trace.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fu yuan hui
 * @date 2023-08-01 18:40:11 Tuesday
 */
@AllArgsConstructor
@Getter
public enum CategoryScopeEnum {

    HRS(1, "HRS", "组织机构"),

    PRM(2, "PRM", "客商管理"),

    TM(3, "TM", "培训管理"),

    CSS(4, "CSS", "财务管理" ),

    XN(5, "XN", "鉴定管理" ),

    CATEGORY(6, "CATEGORY", "基础数据维护"),

    UNKNOWN(-1, "UNKNOWN", "未知" );


    private final int code;

    private final String category;

    private final String categoryDesc;


    public static CategoryScopeEnum matchByCategory(String appCode) {
        CategoryScopeEnum[] values = values();
        for (CategoryScopeEnum value : values) {
            if (appCode.equals(value.getCategory())) {
                return value;
            }
        }
        return UNKNOWN;
    }

    public static CategoryScopeEnum matchByCode(int code) {
        CategoryScopeEnum[] values = values();
        for (CategoryScopeEnum value : values) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
