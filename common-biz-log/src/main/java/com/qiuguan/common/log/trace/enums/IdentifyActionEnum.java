package com.qiuguan.common.log.trace.enums;

import lombok.Getter;

/**
 * @author fu yuan hui
 * @date 2023-08-22 17:47:47 Tuesday
 */
@Getter
public enum IdentifyActionEnum {

    ADD("add", "新增", LogTypeEnum.ADD),

    EDIT("edit","修改", LogTypeEnum.UPDATE),

    UPDATE("update", "更新", LogTypeEnum.UPDATE),

    SERVICE("service", "客服更新", LogTypeEnum.SERVICE_UPDATE),

    ENTRUST("entrust", "网上委托新增", LogTypeEnum.ADD),

    TEC_UPDATE("tecUpdate", "技术更新", LogTypeEnum.TECH_UPDATE),

    IDENTIFY("identify", "识别", LogTypeEnum.IDENTIFY),

    TEC("tec", "技术", LogTypeEnum.TECHNOLOGY),

    FTRIAL("ftrial", "初审", LogTypeEnum.FIRST_APPROVE),

    TRIAL("trial", "审批", LogTypeEnum.APPROVE),

    COPY("copy", "复制", LogTypeEnum.ADD),

    UNKNOWN("unknown", "未知", LogTypeEnum.UNKNOWN);

    private final String code;

    private final String description;

    private final LogTypeEnum type;

    private

    IdentifyActionEnum(String code, String description, LogTypeEnum type) {
        this.code = code;
        this.description = description;
        this.type = type;
    }

    public static IdentifyActionEnum fromCode(String code) {
        for (IdentifyActionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }

        return UNKNOWN;
    }
}