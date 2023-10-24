package com.qiuguan.common.log.trace.chain;

import com.qiuguan.common.log.trace.bean.LogTypePageNameBean;
import com.qiuguan.common.log.trace.enums.IdentityTypeEnum;
import org.springframework.util.StringUtils;

/**
 * @author fu yuan hui
 * @date 2023-08-23 09:45:58 Wednesday
 */
public interface LogTypePageNameSearchChain {

    /**
     * 检索
     */
    LogTypePageNameBean retrieve();

    default String configPrefix(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return null;
        }

        if (permission.startsWith(IdentityTypeEnum.DA.getTypePrefix())) {
            return IdentityTypeEnum.DA.getBizDesc();
        }

        if (permission.startsWith(IdentityTypeEnum.CH.getTypePrefix())) {
            return IdentityTypeEnum.CH.getBizDesc();
        }

        if (permission.startsWith(IdentityTypeEnum.MT.getTypePrefix())) {
            return IdentityTypeEnum.MT.getBizDesc();
        }

        if (permission.startsWith(IdentityTypeEnum.DE.getTypePrefix())) {
            return IdentityTypeEnum.DE.getBizDesc();
        }

        if (permission.startsWith(IdentityTypeEnum.SDS.getTypePrefix())) {
            return IdentityTypeEnum.SDS.getBizDesc();
        }

        return null;
    }
}
