package com.qiuguan.common.log.trace.interceptor;

import com.alibaba.fastjson.JSON;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fu yuan hui
 * @date 2023-08-03 16:04:29 Thursday
 */
@Component
public class DeleteActionExecuteInterceptor extends AbstractActionExecuteInterceptor {

    @Override
    public boolean supports(LogTypeEnum logTypeEnum) {
        return LogTypeEnum.DELETE == logTypeEnum;
    }

    @Override
    public void log(LogArgs logArgs) throws Exception {

        ServicesLog logBean = createLogBean(logArgs);
        //String deleteContent = resolveReadableSql(resolveSql(logArgs, logBean.getSqlParseMap()));
        logBean.setPageFunction(LogTypeEnum.DELETE.getCode());
        //删除这里应该也不需要内容
        logBean.setLogUpdateRemark("");
        logBean.setBusinessScope("delete");

        saveLog(logBean);
    }

    /**
     * DELETE FROM category_instrument WHERE id = 5 and age > 3;
     * @param sql
     * @return
     */
    @Override
    protected String resolveReadableSql(String sql) {
        Map<String, String> map = new HashMap<>();
        sql = sql.toLowerCase();
        //有可能是全表删除
        if (sql.contains("where")) {
            sql = sql.substring(sql.indexOf("where") + 5);
            String[] x = sql.trim().split("and");
            for (String s : x) {
                String[] split = s.split("=");
                if (!StringUtils.isEmpty(split[0])) {
                    map.put(split[0], StringUtils.isEmpty(split[1]) ? "" : split[1]);
                }
            }
        }

        return CollectionUtils.isEmpty(map) ? "" : JSON.toJSONString(map);
    }

}
