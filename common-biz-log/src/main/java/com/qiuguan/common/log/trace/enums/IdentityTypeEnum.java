package com.qiuguan.common.log.trace.enums;

import lombok.Getter;

/**
 * @author fu yuan hui
 * @date 2023-09-04 12:40:29 Monday
 */
@Getter
public enum IdentityTypeEnum {

    DA("da", "危险品"),

    MT("mt", "多种运输"),

    DE("de", "检测业务"),

    CH("ch", "危险特性"),

    SDS("sds", "SDS"),

    ;


    private final String typePrefix;

    private final String bizDesc;

    IdentityTypeEnum(String typePrefix, String bizDesc) {
        this.typePrefix = typePrefix;
        this.bizDesc = bizDesc;
    }

    public static IdentityTypeEnum fromPrefix(String prefix) {
        for (IdentityTypeEnum value : values()) {
            if (value.typePrefix.equals(prefix)) {
                return value;
            }
        }

        return null;
    }
}
