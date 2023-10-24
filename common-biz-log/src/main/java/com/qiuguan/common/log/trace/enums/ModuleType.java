package com.qiuguan.common.log.trace.enums;

import lombok.Getter;

/**
 * @author fu yuan hui
 * @date 2023-08-25 11:18:40 Friday
 */
@Getter
public enum ModuleType {

    DAO(0, "DAO层日志记录"),

    CONTROLLER(1, "Controller层日志记录"),
    ;

    private final int code;

    private final String desc;

    ModuleType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
