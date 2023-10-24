package com.qiuguan.common.log.trace.chain;


import com.qiuguan.common.log.trace.ann.ChainOrder;
import com.qiuguan.common.log.trace.bean.LogTypePageNameBean;
import com.qiuguan.common.log.trace.bean.PermissionBean;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fu yuan hui
 * @date 2023-08-23 09:52:03 Wednesday
 */
@ChainOrder(order = 2)
@Slf4j
@AllArgsConstructor
public class SingletonUrlMappingMultiUseSearcher implements LogTypePageNameSearchChain {

    private final LogTypePageNameSearchChain logTypePageNameSearchChain;

    private final PermissionBean p;

    /**
     * 因为一个url对应多个业务逻辑的工作已经提前到 {@link MappingUrlMarkSearcher} 中进行处理了，所以这里原有的业务逻辑
     * 直接remove掉即可，留一个兜底的匹配规则。
     */
    @Override
    public LogTypePageNameBean retrieve() {

        log.info("执行兜底的动作匹配规则：url: {}", p.getMappingUrl());

        //这里实际上已经不会走了, 因为他就是null, 但这里暂时还是先保持这样的写法
        if (logTypePageNameSearchChain != null) {
            return logTypePageNameSearchChain.retrieve();
        }

        //不继续匹配了，直接返回，作为一个兜底的匹配规则
        return new LogTypePageNameBean(LogTypeEnum.matchByCode(p.getUrlCrud()), getPageName());
    }


    private String getPageName() {
        String prefix = configPrefix(p.getPermission());
        if (org.springframework.util.StringUtils.isEmpty(prefix)) {
            return p.getPermissionName();
        }
        return prefix + " - " + p.getPermissionName();
    }
}
